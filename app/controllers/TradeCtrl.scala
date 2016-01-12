package controllers

import java.util.Date
import javax.inject._

import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.model.account.RealNameInfo
import controllers.security.AuthenticatedAction
import com.lvxingpai.model.marketplace.order.OrderActivity
import core.api.{ CommodityAPI, OrderAPI, TravellerAPI }
import core.exception.ResourceNotFoundException
import core.formatter.marketplace.order.{ OrderFormatter, OrderStatusFormatter, SimpleOrderFormatter, TravellersFormatter }
import core.misc.HanseResult
import core.misc.Implicits._
import core.service.MQService
import org.joda.time.format.DateTimeFormat
import play.api.Configuration
import play.api.libs.json._
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by topy on 2015/10/22.
 */
@Singleton
class TradeCtrl @Inject() (@Named("default") configuration: Configuration, datastore: MorphiaMap) extends Controller {

  implicit lazy val ds = datastore.map.get("k2").get

  val dateFmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  /**
   * 创建订单
   * @return 返回订单信息
   */
  def createOrder() = AuthenticatedAction.async2(
    request => {
      val userId = (request.headers get "UserId" getOrElse "").toLong
      val ret = for {
        body <- request.body.wrapped.asJson
        commodityId <- (body \ "commodityId").asOpt[Long]
        planId <- (body \ "planId").asOpt[String]
        rendezvousTime <- (body \ "rendezvousTime").asOpt[Long]
        quantity <- (body \ "quantity").asOpt[Int]
        travellers <- (body \ "travellers").asOpt[Array[String]]
        comment <- (body \ "comment").asOpt[String] orElse Option("")
      } yield {
        val date = new Date(rendezvousTime)
        val contact = TravellersFormatter.instance.parse[RealNameInfo]((body \ "contact").asInstanceOf[JsDefined].value.toString())
        for {
          tls <- TravellerAPI.getTravellerByKeys(userId, travellers.toSeq)
          order <- CommodityAPI.createOrder(commodityId, planId, date, userId, tls.getOrElse(Seq()), contact, quantity, comment)
        } yield {
          if (order.isEmpty)
            HanseResult.unprocessableWithMsg(Some("下单失败,订单不存在或商品计划选择不正确。"))
          else {
            val node = OrderFormatter.instance.formatJsonNode(order)
            MQService.sendMessage(node.toString, "viae.event.marketplace.onCreateOrder")
            HanseResult(data = Some(node))
          }
        }
      } recover {
        case e: ResourceNotFoundException =>
          // 出现任何失败的情况
          HanseResult.unprocessable(errorMsg = Some(e.getMessage))
        case _ => HanseResult.unprocessableWithMsg(Some("下单失败。"))
      }
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
  def getOrderInfo(orderId: Long) = AuthenticatedAction.async2(
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
  def getOrderStatus(orderId: Long) = AuthenticatedAction.async2(
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
  def getOrders(userId: Long, status: Option[String], start: Int, count: Int) = AuthenticatedAction.async2(
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
  def operateOrder(orderId: Long) = AuthenticatedAction.async2(
    request => {
      val ret = for {
        body <- request.body.wrapped.asJson
        action <- (body \ "action").asOpt[String]
        data1 <- (body \ "data").asOpt[JsObject] orElse Some(JsObject.apply(Seq()))
      } yield {
        val data = Map(data1.fields map (entry => {
          val key = entry._1
          if (key == "userId")
            key -> entry._2.asInstanceOf[JsNumber].value.toInt
          else {
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
            val filterValue = key match {
              case "amount" => (value.asInstanceOf[Double] * 100).toInt
              case _ => value
            }
            key -> filterValue
          }
        }): _*)
        val rr = action match {
          case c if c == OrderActivity.Action.cancel.toString => OrderAPI.setCancel(orderId, data)
          case r if r == OrderActivity.Action.refundApply.toString => OrderAPI.setRefundApplied(orderId, data)
          case e if e == OrderActivity.Action.expire.toString => OrderAPI.setExpire(orderId, data)
          case f if f == OrderActivity.Action.finish.toString => OrderAPI.setFinish(orderId, data)
          case _ => Future {
            throw ResourceNotFoundException(s"Invalid action:$action")
          }
        }
        rr map (x => {
          HanseResult.ok()
        }) recover {
          case e: ResourceNotFoundException => HanseResult.notFound(Some(e.getMessage))
        }
      }
      ret.getOrElse(Future {
        HanseResult.unprocessable()
      })
    }
  )

  //  def operateOrderAct(userId: Long, orderId: Long, action: String, status: String,
  //                      data: Map[String, Any] = Map()): Future[Result] = {
  //    val act = new OrderActivity
  //    act.action = action
  //    act.timestamp = DateTime.now().toDate
  //    act.data = data.asJava
  //
  //    OrderAPI.getOrder(orderId) map (orderOp => {
  //      val order = orderOp.get
  //      val status = order.status
  //
  //      val ret = if (action.equals("canceled") {
  //        // 对准备付款或已付款的订单，可以取消
  //        val rr = if (status.equals(OrderStatus.Pending) || status.equals(OrderStatus.Paid)) {
  //          OrderAPI.updateOrderStatus(orderId, status, act) flatMap  (_ => Future{HanseResult.ok()})
  //        }else
  //        // HanseResult.forbidden(None,Some(s"Order can not canceled. Order status is $status"))
  //          Future{HanseResult.notFound(Some(s"Order can not canceled. Order status is $status"))}
  //        rr
  //        //对已付款或已发货的订单，可以申请退款
  //      }else if (action.equals("refundApplied")) {
  //        if (status.equals(OrderStatus.Paid) || status.equals(OrderStatus.Committed))
  //          Future {
  //            HanseResult.ok()
  //          }
  //        else
  //          Future {
  //            HanseResult.notFound(Some(s"Order can not canceled. Order status is $status"))
  //          }
  //      } else
  //        Future {
  //          HanseResult.notFound(Some(s"Order can not canceled. Order status is $status"))
  //        }
  //      Option(ret)
  //    })

  //    for {
  //      OrderAPI.getOrder()
  //        update <- OrderAPI.updateOrderStatus(orderId, status, act)
  //      order <- OrderAPI.getOrderOnlyStatus(orderId) if update != null
  //    } yield {
  //      val node = OrderStatusFormatter.instance.formatJsonNode(order)
  //      HanseResult(data = Some(node))
  //    }

  //  }
}
