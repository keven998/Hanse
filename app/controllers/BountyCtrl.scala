package controllers

import java.util.ConcurrentModificationException
import javax.inject.{ Inject, Named }

import com.fasterxml.jackson.databind.ObjectMapper
import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.model.account.RealNameInfo
import com.lvxingpai.model.marketplace.order.OrderActivity
import com.lvxingpai.yunkai.UserInfoProp
import controllers.security.AuthenticatedAction
import core.api.{ BountyAPI, SellerAPI }
import core.exception.{ GeneralPaymentException, OrderStatusException, ResourceNotFoundException }
import core.formatter.marketplace.order._
import core.misc.HanseResult
import core.misc.Implicits._
import core.payment.PaymentService.Provider
import core.payment._
import core.service.ViaeGateway
import org.joda.time.DateTime
import play.api.libs.json._
import play.api.mvc.{ Action, Controller, Result, Results }
import play.api.{ Configuration, Logger, Play }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try
import scala.xml.Elem

/**
 * 对于用户悬赏的相关操作
 *
 * Created by topy on 2016/3/29.
 */
class BountyCtrl @Inject() (@Named("default") configuration: Configuration, datastore: MorphiaMap,
    implicit val viaeGateway: ViaeGateway) extends Controller {

  import com.lvxingpai.yunkai.Userservice.{ FinagledClient => YunkaiClient }
  import play.api.Play.current

  val yunkai = Play.application.injector instanceOf classOf[YunkaiClient]

  implicit lazy val ds = datastore.map.get("k2").get

  /**
   * 创建悬赏
   *
   * @return 返回订单信息
   */
  def createBounty() = AuthenticatedAction.async2(
    request => {
      val userId = (request.headers get "X-Lvxingpai-Id" getOrElse "").toLong
      val ret = for {
        body <- request.body.wrapped.asJson
        destination <- (body \ "destination").asOpt[Array[TempLocality]]
        departure <- (body \ "departure").asOpt[TempLocality]
        departureDate <- (body \ "departureDate").asOpt[String]
        timeCost <- (body \ "timeCost").asOpt[Int]
        participantCnt <- (body \ "participantCnt").asOpt[Int]
        participants <- (body \ "participants").asOpt[Seq[String]] orElse Option(Seq())
        perBudget <- (body \ "budget").asOpt[Int]
        service <- (body \ "service").asOpt[String]
        topic <- (body \ "topic").asOpt[String]
        memo <- (body \ "memo").asOpt[String]
        bountyPrice <- (body \ "bountyPrice").asOpt[Float]
      } yield {
        // 出发时间
        val date = DateTime.parse(departureDate).toDate
        val contact = TravellersFormatter.instance.parse[RealNameInfo]((body \ "contact").asInstanceOf[JsDefined].value.toString())
        for {
          userInfo <- TwitterConverter.twitterToScalaFuture(yunkai.getUserById(userId, Some(Seq(UserInfoProp.UserId, UserInfoProp.NickName, UserInfoProp.Avatar))))
          bounty <- BountyAPI.createBounty(userId, userInfo, contact, destination.toSeq, departure, date, timeCost, participantCnt, perBudget, participants,
            service, topic, memo, (bountyPrice * 100).toInt)
        } yield HanseResult(data = Some(BountyFormatter.instance.formatJsonNode(bounty)))
      } recover {
        case e: ResourceNotFoundException => HanseResult.unprocessable(errorMsg = Some(e.getMessage))
      }
      ret getOrElse Future {
        HanseResult.unprocessable()
      }
    }
  )

  /**
   * 取得悬赏列表
   *
   * @return 返回订单信息
   */
  def getBounty(bountyId: Long) = AuthenticatedAction.async2(
    request => {
      val ret = for {
        bounty <- BountyAPI.getBounty(bountyId)
      } yield {
        HanseResult(data = Some(BountyFormatter.instance.formatJsonNode(bounty)))
      }
      ret
    }
  )

  /**
   * 取得悬赏列表
   *
   * @return 返回订单信息
   */
  def getBounties(userId: Option[Long], sortBy: String, sort: String, start: Int, count: Int) = AuthenticatedAction.async2(
    request => {
      val ret = for {
        bounty <- BountyAPI.getBounties(userId, sortBy, sort, start, count)
      } yield {
        HanseResult(data = Some(SimpleBountyFormatter.instance.formatJsonNode(bounty)))
      }
      ret
    }
  )

  def getMyBounties(userId: Long, sortBy: String, sort: String, start: Int, count: Int) = getBounties(Some(userId), sortBy, sort, start, count)

  /**
   * 添加日程安排
   *
   * @param bountyId
   * @return
   */
  def addSchedule(bountyId: Long) = AuthenticatedAction.async2(
    request => {
      val userId = (request.headers get "X-Lvxingpai-Id" getOrElse "").toLong
      val ret = for {
        body <- request.body.wrapped.asJson
        desc <- (body \ "desc").asOpt[String]
        guideId <- Option((body \ "guideId").asOpt[String])
        price <- (body \ "price").asOpt[Float]
      } yield {
        for {
          seller <- SellerAPI.getSeller(userId, Seq("sellerId", "userInfo", "name"))
          userInfo <- TwitterConverter.twitterToScalaFuture(yunkai.getUserById(userId, Some(Seq(UserInfoProp.UserId, UserInfoProp.NickName, UserInfoProp.Avatar))))
          guide <- BountyAPI.getGuide(guideId getOrElse "")
          _ <- BountyAPI.addSchedule(bountyId, seller, guide, userInfo, desc, (price * 100).toInt)
        } yield HanseResult.ok()
      } recover {
        case e: ResourceNotFoundException => HanseResult.unprocessable(errorMsg = Some(e.getMessage))
      }
      ret getOrElse Future {
        HanseResult.unprocessable()
      }
    }
  )

  /**
   * 取得某个悬赏的所有回复
   *
   * @param bountyId
   * @return
   */
  def getSchedules(bountyId: Long) = AuthenticatedAction.async2(
    request => {
      val ret = for {
        schedules <- BountyAPI.getSchedules(bountyId)
      } yield HanseResult(data = Some(ScheduleFormatter.instance.formatJsonNode(schedules)))
      ret
    }
  )

  /**
   * 取得某个悬赏的所有回复
   *
   * @param bountyId
   * @return
   */
  def getScheduleById(bountyId: Long, scheduleId: Long) = AuthenticatedAction.async2(
    request => {
      val ret = for {
        schedules <- BountyAPI.getScheduleById(bountyId, scheduleId)
      } yield HanseResult(data = Some(ScheduleFormatter.instance.formatJsonNode(schedules)))
      ret
    }
  )

  /**
   * 商家接单
   *
   *
   * @return 返回订单信息
   */
  def takeBounty(bountyId: Long) = AuthenticatedAction.async2(
    request => {
      val userId = (request.headers get "X-Lvxingpai-Id" getOrElse "").toLong
      val ret = for {
        body <- request.body.wrapped.asJson
      } yield {
        for {
          userInfo <- TwitterConverter.twitterToScalaFuture(yunkai.getUserById(userId, Some(Seq(UserInfoProp.UserId, UserInfoProp.NickName, UserInfoProp.Avatar))))
          _ <- BountyAPI.setTakers(bountyId, userInfo)
        } yield HanseResult.ok()
      } recover {
        case e: ResourceNotFoundException => HanseResult.unprocessable(errorMsg = Some(e.getMessage))
      }
      ret getOrElse Future {
        HanseResult.unprocessable()
      }
    }
  )

  /**
   * 用户选定商家提供的行程安排并下单
   *
   * @return 返回订单信息
   */
  def createBountyOrder(bountyId: Long) = AuthenticatedAction.async2(
    request => {
      val userId = (request.headers get "X-Lvxingpai-Id" getOrElse "").toLong
      val ret = for {
        body <- request.body.wrapped.asJson
        scheduleId <- (body \ "scheduleId").asOpt[Long]
      } yield {
        for {
          _ <- BountyAPI.orderBounty(bountyId, scheduleId)
          bounty <- BountyAPI.getBounty(bountyId, Seq("schedules"), Some(false))
        } yield {
          HanseResult(data = Some(SimpleBountyFormatter.instance.formatJsonNode(bounty)))
        }
      } recover {
        case e: ResourceNotFoundException =>
          HanseResult.unprocessable(errorMsg = Some(e.getMessage))
      }
      ret.getOrElse(Future {
        HanseResult.unprocessable()
      })
    }
  )

  /**
   * 创建支付宝payment
   *
   * @param orderId
   * @param ip
   * @param userId
   * @return
   */
  def createAlipayPayment(target: String, orderId: Long, ip: String, userId: Long): Future[Result] = {
    val instance = target match {
      case "bounty" => BountyPayAli.instance
      case "schedule" => SchedulePayAli.instance
      case _ => throw ResourceNotFoundException(s"Cannot find target #$target")
    }

    instance.getPrepay(orderId, target) map (entry => {
      val sidecar = entry._2
      val node = new ObjectMapper().createObjectNode()
      node.put("requestString", sidecar("requestString").toString)
      HanseResult.ok(data = Some(node))
    }) recover {
      case e: ResourceNotFoundException => HanseResult.notFound(Some(e.getMessage))
    }
  }

  def createWeChatPayment(target: String, orderId: Long, ip: String, userId: Long): Future[Result] = {
    val instance = target match {
      case "bounty" => BountyPayWeChat.instance
      case "schedule" => SchedulePayWeChat.instance
      case _ => throw ResourceNotFoundException(s"Cannot find target #$target")
    }

    instance getPrepay (orderId, target) map (entry => {
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
   * @param bountyId
   * @return
   */
  def createPayments(bountyId: Long) = AuthenticatedAction.async2(
    request => {
      // 获得客户端的ip地址. 如果不是有效地ipv4地址, 则使用192.168.1.1
      val ip = {
        val ip = request.remoteAddress
        val ipv4Pattern = "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
        if (ip matches ipv4Pattern) ip else "192.168.1.1"
      }
      (for {
        body <- request.body.wrapped.asJson
        userId <- request.headers.get("X-Lvxingpai-Id") map (_.toLong)
        provider <- (body \ "provider").asOpt[String]
        target <- (body \ "target").asOpt[String]
      } yield {
        (provider match {
          case s if s == Provider.Alipay.toString => createAlipayPayment(target, bountyId: Long, ip: String, userId: Long)
          case s if s == Provider.WeChat.toString => createWeChatPayment(target, bountyId: Long, ip: String, userId: Long)
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
        val paymentData: Map[String, String] = body.head.child map { x => x.label.toString -> x.text.toString } filter
          (c => c._1 != "#PCDATA") toMap

        BountyPayWeChat.instance.handleCallback(paymentData) map
          (contents => Ok(contents.asInstanceOf[Elem])) recover {
            case e @ (_: OrderStatusException | _: ConcurrentModificationException) =>
              // 订单状态有误, 或者存在并发修改的情况
              HanseResult.conflict(Some(e.getMessage))
            case e: ResourceNotFoundException =>
              HanseResult.notFound(Some(e.getMessage))
            case e: GeneralPaymentException =>
              // 出现任何失败的情况
              HanseResult.unprocessable(errorMsg = Some(e.getMessage))
          }
      }
      ret getOrElse Future {
        Ok(BountyPayWeChat.wechatCallBackError)
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

        BountyPayWeChat.instance.handleCallback(formData) map (contents => {
          Results.Ok(contents.toString)
        }) recover {
          case e @ (_: OrderStatusException | _: ConcurrentModificationException) =>
            // 订单状态有误, 或者存在并发修改的情况
            HanseResult.conflict(Some(e.getMessage))
          case e: ResourceNotFoundException =>
            HanseResult.notFound(Some(e.getMessage))
          case e: GeneralPaymentException =>
            // 出现任何失败的情况
            HanseResult.unprocessable(errorMsg = Some(e.getMessage))
        }
      }) getOrElse Future {
        HanseResult.unprocessable()
      }
  }

  /**
   * 退款
   *
   * @param bountyId
   * @return
   */
  def refund(bountyId: Long) = AuthenticatedAction.async2(
    request => {
      val r = for {
        body <- request.body.wrapped.asJson
        userId <- request.headers.get("X-Lvxingpai-Id") map (_.toLong)
        memo <- (body \ "memo").asOpt[String] orElse Option("")
        target <- (body \ "target").asOpt[String]
      } yield {
        // 如果没设置退款金额，按照总价退款
        val value = (body \ "refundFee").asOpt[Float] match {
          case None => None
          case x => Some((x.get * 100).toInt)
        }

        (for {
          bounty <- BountyAPI.fetchBounty(bountyId)
          _ <- {
            val tmp = Map("memo" -> memo)
            val data = if (value.nonEmpty)
              tmp + ("amount" -> value.get)
            else
              tmp
            BountyAPI.refundApprove(userId, Some(data), bounty, target)
          }
        } yield {
          HanseResult.ok()
        }) recover {
          case e: ResourceNotFoundException => HanseResult.notFound(Some(e.getMessage))
          case e @ (_: OrderStatusException | _: ConcurrentModificationException) =>
            HanseResult.conflict(Some(e.getMessage))
        }
      }
      r getOrElse Future(HanseResult.unprocessable())
    }
  )

  /**
   * 操作悬赏单-退款申请
   * @param orderId
   * @return
   */
  def operateBounty(orderId: Long) = AuthenticatedAction.async2(
    request => {
      import OrderActivity.Action

      val ret = for {
        body <- request.body.wrapped.asJson
        action <- (body \ "action").asOpt[String] flatMap (v => {
          Try(Some(Action withName v)) getOrElse None
        })
        data <- (body \ "data").asOpt[JsObject] orElse Some(JsObject(Seq())) map procOperateBountyData
      } yield {
        val future = for {
          bounty <- BountyAPI.fetchBounty(orderId)
          _ <- {
            action match {
              case a @ (Action.refundApply) =>
                BountyAPI.refundApply(bounty)
            }
          }
        } yield {
          HanseResult.ok()
        }
        // 有几种异常情况需要处理:
        // * 订单不存在
        // * 订单状态有误
        future recover {
          case e: ResourceNotFoundException => HanseResult.notFound(Some(e.getMessage))
          case e @ (_: OrderStatusException | _: ConcurrentModificationException) =>
            HanseResult.conflict(Some(e.getMessage))
          case _: MatchError => HanseResult.unprocessableWithMsg(Some(s"Invalid action: $action"))
        }
      }
      ret.getOrElse(Future {
        HanseResult.unprocessable()
      })
    }
  )

  /**
   * 解析 operateBounty 参数
   * @param data
   * @return
   */
  private def procOperateBountyData(data: JsObject): Map[String, Any] = {
    val kvSeq = data.fields map {
      case ("amount", v: JsNumber) if v.value.isDecimalFloat => ("amount", (v.value.floatValue() * 100).toInt)
      case ("userId", v: JsNumber) if v.value.isValidLong => ("userId", v.value.longValue())
      case (k, v: JsNumber) if v.value.isValidInt => (k, v.value.intValue())
      case (k, v: JsNumber) if v.value.isValidLong => (k, v.value.longValue())
      case (k, v: JsNumber) if v.value.isDecimalFloat => (k, v.value.floatValue())
      case (k, v: JsNumber) if v.value.isDecimalDouble => (k, v.value.doubleValue())
      case (k, v: JsBoolean) => (k, v.value)
      case (k, v: JsString) => (k, v.value)
      case (k, JsNull) => (k, null)
      case (k, _) => (k, JsNull) // 这一类数据稍后会被清除
    } filterNot {
      case (k, v) => v == JsNull
    }
    Map(kvSeq: _*)
  }

}
