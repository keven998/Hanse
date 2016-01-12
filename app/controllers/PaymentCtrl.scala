package controllers

import javax.inject.{ Inject, Named, Singleton }

import com.fasterxml.jackson.databind.ObjectMapper
import com.lvxingpai.inject.morphia.MorphiaMap
import controllers.security.AuthenticatedAction
import core.exception.{ GeneralPaymentException, ResourceNotFoundException }
import core.exception.{ GeneralPaymentException, OrderStatusException, ResourceNotFoundException }
import core.misc.HanseResult
import core.misc.Implicits._
import core.payment.PaymentService.Provider
import core.payment.{ AlipayService, WeChatPaymentService }
import play.api.mvc.{ Action, Controller, Result, Results }
import play.api.{ Configuration, Logger }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps
import scala.xml.Elem

@Singleton
class PaymentCtrl @Inject() (@Named("default") configuration: Configuration, datastore: MorphiaMap) extends Controller {

  implicit lazy val ds = datastore.map.get("k2").get

  /**
   * 创建支付宝payment
   *
   * @param orderId
   * @param ip
   * @param userId
   * @return
   */
  def createAlipayPayment(orderId: Long, ip: String, userId: Long): Future[Result] = {
    val instance = AlipayService.instance
    instance.getPrepay(orderId) map (entry => {
      val sidecar = entry._2
      val node = new ObjectMapper().createObjectNode()
      node.put("requestString", sidecar("requestString").toString)
      HanseResult.ok(data = Some(node))
    }) recover {
      case e: ResourceNotFoundException => HanseResult.notFound(Some(e.getMessage))
    }
  }

  def createWeChatPayment(orderId: Long, ip: String, userId: Long): Future[Result] = {
    val instance = WeChatPaymentService.instance

    instance getPrepay orderId map (entry => {
      val node = new ObjectMapper().createObjectNode()
      val sidecar = entry._2
      sidecar foreach (entry => {
        val key = entry._1
        val value = entry._2
        node.put(key, value.toString)
      })
      HanseResult.ok(data = Some(node))
    }) recover {
      case e: ResourceNotFoundException => HanseResult.notFound(Some(e.getMessage))
    }
  }

  /**
   * 生成预支付对象
   *
   * @param orderId
   * @return
   */
  def createPayments(orderId: Long) = AuthenticatedAction.async2(
    request => {
      // 获得客户端的ip地址. 如果不是有效地ipv4地址, 则使用192.168.1.1
      val ip = {
        val ip = request.remoteAddress
        val ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
        if (ip matches ipv4Pattern) ip else "192.168.1.1"
      }

      (for {
        body <- request.body.wrapped.asJson
        userId <- request.headers.get("UserId") map (_.toLong)
        provider <- (body \ "provider").asOpt[String]
      } yield {
        (provider match {
          case s if s == Provider.Alipay.toString => createAlipayPayment(orderId: Long, ip: String, userId: Long)
          case s if s == Provider.WeChat.toString => createWeChatPayment(orderId: Long, ip: String, userId: Long)
          case _ => Future(HanseResult.unprocessable(errorMsg = Some(s"Invalid provider: $provider")))
        }) recover {
          case e: Throwable =>
            // TODO 确定合适的HTTP status code
            HanseResult.unprocessable(errorMsg = Some(e.getMessage))
        }
      }) getOrElse Future(HanseResult.unprocessable())
    }
  )

  /**
   * 微信的回调接口
   * @return
   */
  def wechatCallback() = Action.async(
    request => {
      val ret = for {
        body <- request.body.asXml
      } yield {

        val prePay: Map[String, String] = body.head.child map { x => x.label.toString -> x.text.toString } filter (c => c._1 != "#PCDATA") toMap

        WeChatPaymentService.instance.handleCallback(prePay) map (contents => Ok(contents.asInstanceOf[Elem])) recover {
          case e: GeneralPaymentException =>
            // 出现任何失败的情况
            HanseResult.unprocessable(errorMsg = Some(e.getMessage))
          case e: ResourceNotFoundException =>
            HanseResult.notFound(Some(e.getMessage))
        }
      }
      ret getOrElse Future {
        Ok(WeChatPaymentService.wechatCallBackError)
      }
    }
  )

  /**
   * 支付宝的回调接口
   * @return
   */
  def alipayCallback() = Action.async {
    request =>
      (for {
        formData <- request.body.asFormUrlEncoded
      } yield {
        val notifyId = formData("notify_id") mkString ","
        val tradeId = formData("out_trade_no") mkString ","
        val tradeStatus = formData("trade_status") mkString ","
        val buyer = formData("buyer_email") mkString ","
        val totalFee = formData("total_fee") mkString ","

        Logger.info(s"Alipay callback: notify_id=$notifyId out_trade_no=$tradeId trade_status=$tradeStatus " +
          s"buyer_email=$buyer total_fee=$totalFee")

        AlipayService.instance.handleCallback(formData) map (contents => {
          Results.Ok(contents.toString)
        }) recover {
          case e: GeneralPaymentException =>
            // 出现任何失败的情况
            HanseResult.unprocessable(errorMsg = Some(e.getMessage))
          case e: ResourceNotFoundException =>
            HanseResult.notFound(Some(e.getMessage))
        }
      }) getOrElse Future {
        HanseResult.unprocessable()
      }
  }

  def refund(orderId: Long) = AuthenticatedAction.async2(
    request => {
      val r = for {
        body <- request.body.wrapped.asJson
        userId <- request.headers.get("UserId") map (_.toLong)
      } yield {
        // 如果没设置退款金额，按照总价退款
        val value = (body \ "refundFee").asOpt[Float] match {
          case None => None
          case x => Some((x.get * 100).toInt)
        }
        WeChatPaymentService.instance.refund(userId, orderId, value) map (_ => HanseResult.ok()) recover {
          // 错误码与商家系统对应
          case e: ResourceNotFoundException => HanseResult.notFound(Some(e.getMessage))
          case e: OrderStatusException => HanseResult.notFound(Some(e.getMessage))
        }
      }
      r getOrElse Future(HanseResult.unprocessable())
    }
  )

}
