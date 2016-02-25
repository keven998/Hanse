package controllers

import java.util.ConcurrentModificationException
import javax.inject._

import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.model.account.RealNameInfo
import com.lvxingpai.model.marketplace.order.OrderActivity
import controllers.security.AuthenticatedAction
import core.api._
import core.exception.{ OrderStatusException, ResourceNotFoundException }
import core.formatter.marketplace.order._
import core.misc.HanseResult
import core.service.ViaeGateway
import org.bson.types.ObjectId
import org.joda.time.format.{ DateTimeFormat, ISODateTimeFormat }
import play.api.Configuration
import play.api.libs.json._
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

/**
 * Created by topy on 2015/10/22.
 */
@Singleton
class TradeCtrl @Inject() (@Named("default") configuration: Configuration, datastore: MorphiaMap,
    implicit val viaeGateway: ViaeGateway) extends Controller {

  implicit lazy val ds = datastore.map.get("k2").get

  val dateFmt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")

  /**
   * 创建订单
   * @return 返回订单信息
   */
  def createOrder() = AuthenticatedAction.async2(
    request => {
      val userId = (request.headers get "X-Lvxingpai-Id" getOrElse "").toLong
      val ret = for {
        body <- request.body.wrapped.asJson
        commodityId <- (body \ "commodityId").asOpt[Long]
        planId <- (body \ "planId").asOpt[String]
        rendezvousTime <- (body \ "rendezvousTime").asOpt[String]
        quantity <- (body \ "quantity").asOpt[Int]
        travellers <- (body \ "travellers").asOpt[Array[String]]
        comment <- (body \ "comment").asOpt[String] orElse Option("")
        coupons <- (body \ "coupons").asOpt[Seq[String]] orElse Option(Seq())
      } yield {
        // 读取预约时间
        val date = ISODateTimeFormat.date parseLocalDate rendezvousTime
        val contact = TravellersFormatter.instance.parse[RealNameInfo]((body \ "contact").asInstanceOf[JsDefined].value.toString())
        for {
          tls <- TravellerAPI.getTravellerByKeys(userId, travellers.toSeq)
          order <- {
            // 优惠券
            val couponList = coupons map (v => Try(new ObjectId(v)).toOption) filter (_.nonEmpty) map (_.get)
            CommodityAPI.createOrder(commodityId, planId, date, userId, tls.getOrElse(Seq()), contact, quantity,
              comment, couponList)
          }
        } yield {
          if (order.isEmpty)
            HanseResult.unprocessableWithMsg(Some("下单失败,订单不存在或商品计划选择不正确。"))
          else {
            val node = OrderFormatter.instance.formatJsonNode(order)
            HanseResult(data = Some(node))
          }
        }
      } recover {
        case e: ResourceNotFoundException =>
          // 出现任何失败的情况
          HanseResult.unprocessable(errorMsg = Some(e.getMessage))
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
      val callerId = request.headers get "X-Lvxingpai-Id"
      if (callerId.isEmpty) Future(HanseResult.forbidden())
      else {
        for {
          opt <- OrderAPI.getOrder(orderId)
        } yield {
          opt map (order => {
            val order = opt.get
            // 只有买家和卖家双方可以看到订单内容
            if (callerId.get == order.consumerId.toString || callerId.get == order.commodity.seller.sellerId.toString) {
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
      val callerId = request.headers get "X-Lvxingpai-Id"
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
      val callerId = request.auth.user map (_.userId)
      if (callerId.isEmpty || callerId.get != userId) {
        // 权限检查
        Future.successful(HanseResult.forbidden())
      } else {
        val formatter = SimpleOrderFormatter.instance
        OrderAPI.getOrderList(userId, status, start, count) map (
          formatter.formatJsonNode _ andThen Some.apply andThen (v => HanseResult.ok(data = v))
        )
      }
    }
  )

  /**
   * 处理operateOrder接口中接收到的data数据
   * @param data
   */
  private def procOperateOrderData(data: JsObject): Map[String, Any] = {
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

  /**
   * 操作订单
   *
   * @return
   */
  def operateOrder(orderId: Long) = AuthenticatedAction.async2(
    request => {
      import OrderActivity.Action

      val ret = for {
        body <- request.body.wrapped.asJson
        action <- (body \ "action").asOpt[String] flatMap (v => {
          Try(Some(Action withName v)) getOrElse None
        })
        data <- (body \ "data").asOpt[JsObject] orElse Some(JsObject(Seq())) map procOperateOrderData
      } yield {
        val future = for {
          order <- OrderAPI.fetchOrder(orderId)
          _ <- {
            val statedOrder = StatedOrder(order)

            action match {
              case a @ (Action.cancel | Action.refundApply | Action.expire | Action.finish) =>
                statedOrder.applyAction(a, request.auth.roles, request.auth.user, Some(data))
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
  /**
   * 获取我的优惠卷列表
   *
   * @return
   */
  def getCouponList(userId: Long) = AuthenticatedAction.async2(
    request => {
      for {
        //t <- OrderAPI.createCouponTemp(userId)
        couponList <- OrderAPI.getCouponList(userId)
      } yield {
        HanseResult(data = Some(CouponFormatter.instance.formatJsonNode(couponList)))
      }
    }
  )
}
