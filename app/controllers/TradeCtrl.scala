package controllers

import java.util.Date
import javax.inject._

import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.model.account.RealNameInfo
import com.lvxingpai.model.marketplace.order.OrderActivity
import com.lvxingpai.model.misc.PhoneNumber
import core.api.{ CommodityAPI, OrderAPI, TravellerAPI }
import core.formatter.marketplace.order.{ OrderFormatter, OrderStatusFormatter, SimpleOrderFormatter }
import core.misc.HanseResult
import core.misc.Implicits._
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat
import play.api.Configuration
import play.api.libs.json._
import play.api.mvc.{ Action, Controller, Result }

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by topy on 2015/10/22.
 */
@Singleton
class TradeCtrl @Inject() (@Named("default") configuration: Configuration, datastore: MorphiaMap) extends Controller {

  implicit lazy val ds = datastore.map.get("k2").get

  val dateFmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  case class ContactTemp(surname: String, givenName: String, phone: PhoneNumber, email: String) {
    def toContact = {
      val contact = new RealNameInfo
      val tel = new PhoneNumber
      contact.surname = surname
      contact.givenName = givenName
      tel.dialCode = phone.dialCode
      tel.number = phone.number
      contact.tel = tel
      contact.email = email
      contact
    }
  }

  /**
   * 创建订单
   * @return 返回订单信息
   */
  def createOrder() = Action.async(
    request => {
      val ip = request.remoteAddress
      // TODO 为何使用Header中的UserId? 如果没有正确提供UserId, 这里会抛出异常
      val userId = request.headers.get("UserId").getOrElse("").toLong
      val ret = for {
        body <- request.body.asJson
        commodityId <- (body \ "commodityId").asOpt[Long]
        planId <- (body \ "planId").asOpt[String]
        rendezvousTime <- (body \ "rendezvousTime").asOpt[Long]
        quantity <- (body \ "quantity").asOpt[Int]
        travellers <- (body \ "travellers").asOpt[Array[String]]
        phone <- (body \ "contactPhone").asOpt[PhoneNumberTemp] // TODO 为何需要PhoneNumberTemp?
        email <- (body \ "contactEmail").asOpt[String] orElse Option("")
        surname <- (body \ "contactSurname").asOpt[String]
        givenName <- (body \ "contactGivenName").asOpt[String]
        comment <- (body \ "comment").asOpt[String] orElse Option("")
      } yield {
        val date = new Date(rendezvousTime)
        val contact = ContactTemp(surname, givenName, phone, email)
        for {
          // TODO controller中不应该包含业务逻辑
          //commodity <- CommodityAPI.getCommoditySnapsById(commodityId, planId, price, date)
          tls <- TravellerAPI.getTravellerByKeys(userId, travellers.toSeq)
          order <- CommodityAPI.createOrder(commodityId, planId, date, userId, tls.getOrElse(Seq()), contact.toContact, quantity, comment)
        } yield {
          val node = OrderFormatter.instance.formatJsonNode(order)
          HanseResult(data = Some(node))
        }
      }.fallbackTo(Future {
        HanseResult.unprocessableWithMsg(Some("下单失败。"))
      })
      ret.getOrElse(Future {
        HanseResult.unprocessable()
      })
    }
  )

  /**
   * 订单详情
   * @param orderId 订单id
   * @return 订单详情
   */
  def getOrderInfo(orderId: Long) = Action.async(
    request => {
      val callerId = request.headers get "UserId"
      if (callerId.isEmpty) Future(HanseResult.forbidden())
      else {
        for {
          opt <- OrderAPI.getOrder(orderId)
        } yield {
          opt map (order => {
            val order = opt.get
            if (callerId.get == order.consumerId.toString) {
              val node = OrderFormatter.instance.formatJsonNode(order)
              HanseResult(data = Some(node))
            } else {
              HanseResult.forbidden()
            }
          }) getOrElse HanseResult.notFound(Some(s"Invalid order id: $orderId"))
        }
      }
    }
  )

  /**
   * 订单状态
   * @param orderId
   * @return
   */
  def getOrderStatus(orderId: Long) = Action.async(
    request => {
      val callerId = request.headers get "UserId"
      if (callerId.isEmpty) {
        Future(HanseResult.forbidden())
      } else {
        for {
          order <- OrderAPI.getOrderOnlyStatus(orderId)
        } yield {
          if (callerId.get == order.consumerId.toString) {
            val node = OrderStatusFormatter.instance.formatJsonNode(order)
            HanseResult(data = Some(node))
          } else {
            HanseResult.forbidden()
          }
        }
      }
    }
  )

  /**
   * 根据用户id获取订单列表
   * 如果订单状态为空, 获取所在用户下的所有的订单列表
   * 如果订单状态不为空, 获取所在用户下的某个订单状态的订单列表
   * @param userId 用户id
   * @param status 订单状态
   * @return 订单列表
   */
  def getOrders(userId: Long, status: Option[String], start: Int, count: Int) = Action.async(
    request => {
      val callerId = request.headers get "UserId"
      for {
        orders <- {
          // 检查Header中的UserId是否匹配
          if (callerId.isEmpty || callerId.get != userId.toString) Future(Seq())
          else OrderAPI.getOrderList(userId, status, start, count)
        }
      } yield {
        val node = SimpleOrderFormatter.instance.formatJsonNode(orders)
        HanseResult(data = Some(node))
      }
    }
  )

  /**
   * 操作订单
   *
   * @return
   */
  def operateOrder(orderId: Long) = Action.async(
    request => {
      val userId = request.headers.get("UserId").getOrElse("").toLong
      val ret = for {
        body <- request.body.asJson
        action <- (body \ "action").asOpt[String]
        memo <- (body \ "memo").asOpt[String] //备注
        amount <- (body \ "amount").asOpt[Double] // 退款金额
        data1 <- (body \ "data").asOpt[JsObject] orElse Some(JsObject.apply(Seq()))
      } yield {
        val data = Map(data1.fields map (entry => {
          val key = entry._1
          val value = entry._2 match {
            case v: JsNumber =>
              if (v.value.isDecimalDouble)
                v.value.doubleValue()
              else if (v.value.isDecimalFloat)
                v.value.floatValue()
            case v: JsBoolean =>
              v.value
            case v: JsString =>
              v.value
            case JsNull =>
              null
          }
          key -> value
        }): _*)
        action match {
          case "cancel" => operateOrderAct(userId, orderId, action, "canceled", data)
          case "refund" => operateOrderAct(userId, orderId, action, "refundApplied", data)
          case _ => Future(HanseResult.unprocessable())
        }
      }
      ret.getOrElse(Future {
        HanseResult.unprocessable()
      })
    }
  )

  def operateOrderAct(userId: Long, orderId: Long, action: String, status: String,
    data: Map[String, Any] = Map()): Future[Result] = {
    val act = new OrderActivity
    act.action = action
    act.timestamp = DateTime.now().toDate
    act.data = data.asJava
    for {
      update <- OrderAPI.updateOrderStatus(orderId, status, act)
      order <- OrderAPI.getOrderOnlyStatus(orderId) if update != null
    } yield {
      val node = OrderStatusFormatter.instance.formatJsonNode(order)
      HanseResult(data = Some(node))
    }

  }
}
