package controllers

import javax.inject.{ Inject, Named, Singleton }

import com.fasterxml.jackson.databind.ObjectMapper
import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.model.marketplace.trade.PaymentVendor
import core.api.OrderAPI
import core.exception.GeneralPaymentException
import core.misc.HanseResult
import core.misc.Implicits._
import core.model.trade.order.WechatPrepay
import core.payment.PaymentService.Provider
import core.payment.{ AlipayService, WeChatPaymentService }
import core.service.PaymentService
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
    })
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
    })
  }

  /**
   * 生成预支付对象
   *
   * @param orderId
   * @return
   */
  def createPayments(orderId: Long) = Action.async(
    request => {
      // 获得客户端的ip地址. 如果不是有效地ipv4地址, 则使用192.168.1.1
      val ip = {
        val ip = request.remoteAddress
        val ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
        if (ip matches ipv4Pattern) ip else "192.168.1.1"
      }

      (for {
        body <- request.body.asJson
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
        }
      }) getOrElse Future {
        HanseResult.unprocessable()
      }
  }

  def getPaymentsStatus(orderId: Long) = Action.async(
    request => {
      val ret = for {
        orderValue <- OrderAPI.getOrder(orderId)
        wcResponse <- PaymentService.queryOrder(
          Map(WechatPrepay.FD_TRANSACTION_ID ->
            orderValue.paymentInfo.get(PaymentVendor.Wechat).prepayId)
        )
      } yield {
        val str = orderValue.status
        Ok(str)
      }
      ret
    }
  )

  def getPayments(orderId: Long, provider: String) = Action.async(
    request => {
      val ret = for {
        orderValue <- OrderAPI.getOrder(orderId)
        wcResponse <- PaymentService.queryOrder(
          Map(WechatPrepay.FD_TRANSACTION_ID ->
            orderValue.paymentInfo.get(PaymentVendor.Wechat).prepayId)
        )
      } yield {
        val str = orderValue.status
        Ok(str)
      }
      ret
    }
  )

  //  def refund(orderId: Long) = Action.async(
  //    request => {
  //      val r = for {
  //        body <- request.body.asJson
  //        userId <- request.headers.get("UserId") map (_.toLong)
  //        refundFee <- (body \ "refundFee").asOpt[Float]
  //      } yield {
  //          val refundNo = new Date getTime
  //          for {
  //           // od <- OrderAPI.getOrder(orderId, Seq("totalFee"))
  //            ws <- PaymentService.refund(Map("refund_fee" -> refundFee * 100, "total_fee" -> (0.01 * 100).toString
  //              , "out_refund_no" -> refundNo))
  //        } yield {
  //            val ret = new String(ws.bodyAsBytes, "UTF8")
  //            val node = new ObjectMapper().createObjectNode()
  //            node.put("ret", ret)
  //            HanseResult.ok(data = Some(node))
  //          }
  //        }
  //      r getOrElse Future(HanseResult.unprocessable())
  //    }
  //  )

  def refund(orderId: Long) = Action.async(
    request => {
      val r = for {
        body <- request.body.asJson
        userId <- request.headers.get("UserId") map (_.toLong)
        refundFee <- (body \ "refundFee").asOpt[Float]
      } yield {
        for {
          order <- OrderAPI.getOrder(orderId, Seq("totalFee"))
          ws <- PaymentService.refund(Map("refund_fee" -> refundFee * 100, "total_fee" -> (order.totalPrice * 100).toString, "out_trade_no" -> orderId))
        } yield {
          val ret = new String(ws.bodyAsBytes, "UTF8")
          val node = new ObjectMapper().createObjectNode()
          node.put("ret", ret)
          HanseResult.ok(data = Some(node))
        }
      }
      r getOrElse Future(HanseResult.unprocessable())
    }
  )

}
