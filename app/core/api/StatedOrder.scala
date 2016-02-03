package core.api

import java.util.Date

import com.lvxingpai.model.marketplace.order.Order.Status
import com.lvxingpai.model.marketplace.order.Order.Status._
import com.lvxingpai.model.marketplace.order.OrderActivity.Action
import com.lvxingpai.model.marketplace.order.{ Order, OrderActivity }
import com.lvxingpai.yunkai.UserInfo
import core.exception.{ ForbiddenException, OrderStatusException }
import core.formatter.marketplace.order.OrderFormatter
import core.payment.PaymentService.Provider._
import core.payment.{ AlipayService, PaymentService, WeChatPaymentService }
import core.security.UserRole
import core.service.ViaeGateway
import org.joda.time.DateTime
import org.mongodb.morphia.Datastore

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

/**
 * 以状态机模型, 操作订单状态
 *
 * Created by zephyre on 1/22/16.
 */
class StatedOrder(val order: Order)(implicit datastore: Datastore, viae: ViaeGateway) {

  /**
   * 向订单应用一个操作
   * @param action 操作
   * @param roles 操作者的角色
   * @param operator 操作者ID
   * @param data 可选的其它数据
   * @return
   */
  def applyAction(action: Action.Value, roles: Set[UserRole.Value], operator: Option[UserInfo], data: Option[Map[String, Any]] = None): Future[StatedOrder] = {
    import PaymentService.Provider

    val currentStatus = Order.Status withName order.status
    (currentStatus, action) match {
      case (Pending, Action.pay) => data getOrElse Map() get "provider" match {
        case Some(Provider.Alipay) => pay(operator.get.userId, Provider.Alipay)
        case Some(Provider.WeChat) => pay(operator.get.userId, Provider.WeChat)
      }
      case (Pending, Action.cancel) => cancel(operator.get.userId, data)
      case (Pending, Action.expire) => expireOnPending(roles)
      case (Paid, Action.commit) => commit()
      case (Paid, Action.refundApply) => refundApply(operator.get.userId, data)
      case (Paid, Action.expire) => expireAndRefund(roles)
      case (Committed, Action.finish) => finish(roles)
      case (Committed, Action.refundApply) => refundApply(operator.get.userId, data)
      case (RefundApplied, Action.refundApprove) => refundApprove(operator.get.userId, data)
      case (RefundApplied, Action.expire) => expireAndRefund(roles)
      //      case (RefundApplied, Action.refundDeny) => refundDeny(operator)
    }
  }

  /**
   * 将order保存到数据库
   * @return
   */
  def save(): Future[StatedOrder] = Future {
    datastore.save[Order](order)
    this
  }

  /**
   * 追加一个activity
   * @param activity
   * @return
   */
  def applyActivity(activity: OrderActivity): StatedOrder = {
    order.activities = (Option(order.activities) map (_.toSeq :+ activity) getOrElse Seq(activity)).asJava
    order.updateTime = new Date()
    this
  }

  /**
   * 设置订单的状态
   * @param status
   * @return
   */
  def setStatus(status: Status.Value): StatedOrder = {
    order.status = status.toString
    order.updateTime = new Date()
    this
  }

  /**
   * 触发订单操作相关事件
   * @param event
   * @param kwargs
   */
  def emitEvent(event: String, kwargs: Option[Map[String, Any]] = None): Future[String] = {
    val orderNode = OrderFormatter.instance.formatJsonNode(order)
    val taskName = s"viae.event.marketplace.$event"
    val fullKwArgs = Some(kwargs getOrElse Map() + ("order" -> orderNode))
    viae.sendTask(taskName, kwargs = fullKwArgs)
  }

  /**
   * 判断operator是否为管理员
   * @param roles 角色
   */
  def assertAdmin(roles: Set[UserRole.Value], messageBuilder: Option[() => String] = None): Unit = {
    if (!(roles contains UserRole.Admin)) {
      val func = messageBuilder getOrElse (() => s"Only administrators can apply the action")
      throw ForbiddenException(func())
    }
  }

  /**
   * 判断operator是否为商家
   * @param operator
   * @param messageBuilder
   */
  def assertSeller(operator: Long, messageBuilder: Option[(Long) => String] = None): Unit = {
    if (operator != order.commodity.seller.sellerId) {
      val func = messageBuilder getOrElse ((id: Long) => s"Operator $operator is not the seller.")
      throw ForbiddenException(func(operator))
    }
  }

  /**
   * 判断operator是否为商家
   * @param operator
   * @param messageBuilder
   */
  def assertConsumer(operator: Long, messageBuilder: Option[(Long) => String] = None): Unit = {
    if (operator != order.consumerId) {
      val func = messageBuilder getOrElse ((id: Long) => s"Operator $operator is not the consumer.")
      throw ForbiddenException(func(operator))
    }
  }

  /**
   * 判断订单状态是否符合预期
   * @param expected
   * @param messageBuilder
   */
  def assertStatus(expected: Status.Value, messageBuilder: Option[(Status.Value) => String] = None): Unit = {
    val f = messageBuilder map (mb => {
      (values: Seq[Status.Value]) => mb(values.head)
    })
    assertStatuses(Seq(expected), f)
  }

  /**
   * 判断订单状态是否符合预期
   * @param expected
   * @param messageBuilder
   */
  def assertStatuses(expected: Seq[Status.Value], messageBuilder: Option[(Seq[Status.Value]) => String] = None): Unit = {
    val actual = Status withName order.status
    if (!(expected contains actual)) {
      val func = messageBuilder getOrElse ((e: Seq[Status.Value]) => {
        val tmp = e map (_.toString) mkString "or"
        s"Order #${order.orderId}: the expected status is $tmp, the actual status is $actual"
      })
      throw OrderStatusException(func(expected))
    }
  }

  /**
   * 订单成功结束
   * @return
   */
  def finish(roles: Set[UserRole.Value]): Future[StatedOrder] = {
    for {
      newOrder <- {
        assertAdmin(roles)
        assertStatus(Status.Committed)

        val activity = OrderActivity(Action.finish, Status.Committed)
        this applyActivity activity setStatus Status.Finished
        this.save()
      }
    } yield {
      newOrder
    }
  }

  /**
   * 支付订单
   * @return
   */
  def pay(operator: Long, provider: PaymentService.Provider.Value): Future[StatedOrder] = {
    import PaymentService.Provider._

    for {
      newOrder <- {
        assertConsumer(operator)
        assertStatus(Status.Pending)

        // 设置支付状态
        provider match {
          case Alipay =>
            order.paymentInfo(Alipay.toString).paid = true
          case WeChat =>
            order.paymentInfo(WeChat.toString).paid = true
        }
        val activity = OrderActivity(Action.pay, Order.Status.Pending)
        this applyActivity activity setStatus Status.Paid
        order.expireDate = DateTime.now().plusDays(1).toDate

        this.save()
      }
    } yield {
      // 发送支付订单的事件
      emitEvent("onPayOrder")
      newOrder
    }
  }

  /**
   * 申请退款
   * @param data
   * @return
   */
  def refundApply(operator: Long, data: Option[Map[String, Any]]): Future[StatedOrder] = {
    // 处理data
    val newData = data map (m => {
      val keys = Seq("memo", "reason")
      m filterKeys (keys contains _) map {
        case (k, v: String) => k -> v // memo和reason都是String
        case (k, _) => k -> ""
      }
    })
    for {
      newOrder <- {
        // 只有用户可以提交退款申请
        assertConsumer(operator)
        assertStatuses(Seq(Status.Paid, Status.Committed))

        val activity = OrderActivity(Action.refundApply, Status.RefundApplied, newData)
        this applyActivity activity setStatus Status.RefundApplied
        this.save()
      }
    } yield {
      // 发送申请退款的事件
      val (memo, reason) = {
        val tmp = newData getOrElse Map()
        (tmp.getOrElse("memo", ""), tmp.getOrElse("reason", ""))
      }
      emitEvent("onRefundApply", Some(Map("memo" -> memo, "reason" -> reason)))
      newOrder
    }
  }

  /**
   * 在pending状态下的订单失效
   * @return
   */
  def expireOnPending(roles: Set[UserRole.Value]): Future[StatedOrder] = {
    for {
      newOrder <- {
        assertAdmin(roles)
        assertStatus(Pending)

        val activity = OrderActivity(Action.expire, Pending)
        this applyActivity activity setStatus Canceled
        this.save()
      }
    } yield {
      newOrder
    }
  }

  /**
   * 超时并退款. 比如: 付款以后, 商家迟迟不发货, 订单失效, 或者申请退款以后, 商家置之不理
   * @param roles
   * @return
   */
  def expireAndRefund(roles: Set[UserRole.Value]): Future[StatedOrder] = {
    import PaymentService.Provider._

    for {
      newOrder <- {
        assertAdmin(roles)
        assertStatus(Paid)

        val activity = OrderActivity(Action.expire, Paid)
        this applyActivity activity setStatus Refunded

        // 尝试退款
        val amount = order.totalPrice - order.discount
        Option(order.paymentInfo) map (_.toMap) getOrElse Map() foreach {
          case (id: String, prepay) if id == Alipay.toString => AlipayService.instance.refundProcess(order, amount)
          case (id: String, prepay) if id == WeChat.toString => WeChatPaymentService.instance.refundProcess(order, amount)
        }
        this.save()
      }
    } yield {
      newOrder
    }
  }

  /**
   * 商家同意退款
   * @param operator
   * @param data
   * @return
   */
  def refundApprove(operator: Long, data: Option[Map[String, Any]]): Future[StatedOrder] = {
    val amount = data getOrElse Map() get "amount" flatMap (v => {
      (Try(Some(v.toString.toFloat)) recover {
        case _: NumberFormatException => None
      }).get
    }) map (v => (v * 100).toInt) getOrElse (order.totalPrice - order.discount) // 默认情况下, 退还全款
    // 是否存在退款申请
    val withApplication = order.status == RefundApplied.toString

    for {
      newOrder <- {
        assertSeller(operator)
        assertStatus(if (withApplication) RefundApplied else Paid)

        val activity = OrderActivity(Action.refundApprove, Order.Status withName order.status,
          Some(Map("amount" -> amount)))
        this applyActivity activity setStatus Refunded

        // 尝试退款
        Option(order.paymentInfo) map (_.toMap) getOrElse Map() foreach {
          case (id: String, prepay) if id == Alipay.toString => AlipayService.instance.refundProcess(order, amount)
          case (id: String, prepay) if id == WeChat.toString => WeChatPaymentService.instance.refundProcess(order, amount)
        }
        this.save()
      }
    } yield {
      //order, amount, with_application, memo=None
      emitEvent("onRefundApprove", Some(Map("amount" -> amount, "with_application" -> withApplication)))
      newOrder
    }
  }

  //  /**
  //    * 商家拒绝了用户的退款申请
  //    * @param operator
  //    * @return
  //    */
  //  def refundDeny(operator: Long): Future[StatedOrder] = {
  //    for {
  //      newOrder <- {
  //        assertSeller(operator)
  //        assertStatus(RefundApplied)
  //
  //        val activity=OrderActivity(Action.refundDeny, RefundApplied)
  //        this applyActivity activity setStatus Paid
  //      }
  //    }
  //  }

  /**
   * 取消订单. 需要修改以下订单状态: status, 以及activities. 最后, 触发onCancelOrder事件
   * @return
   */
  def cancel(operator: Long, data: Option[Map[String, Any]]): Future[StatedOrder] = {
    // 处理data
    val newData = data map (m => {
      val keys = Seq("memo", "reason", "userId")
      m filterKeys (keys contains _) map {
        case (k, v: String) => k -> v // memo和reason都是String
        case (k, v: Long) => k -> v // userId是String
        case (k, _) => k -> ""
      }
    })

    for {
      newOrder <- {
        assertStatus(Pending)
        // operator必须是商家或买家
        if (operator != order.consumerId && operator != order.commodity.seller.sellerId) {
          throw ForbiddenException(s"The operator $operator is neither the consumer ${order.consumerId} nor the " +
            s"seller ${order.commodity.seller.sellerId}.")
        }

        // 获得新的activity
        val activity = OrderActivity(Action.cancel, Pending, newData)
        this applyActivity activity setStatus Canceled
        this.save()
      }
    } yield {
      // 发送取消订单的事件
      val (memo, reason) = {
        val tmp = newData getOrElse Map()
        (tmp.getOrElse("memo", ""), tmp.getOrElse("reason", ""))
      }
      emitEvent("onCancelOrder", Some(Map("memo" -> memo, "reason" -> reason)))
      newOrder

    }
  }

  def commit(): Future[StatedOrder] = {
    Future.successful(StatedOrder(this.order))
  }

}

object StatedOrder {
  def apply(order: Order)(implicit datastore: Datastore, viae: ViaeGateway) = new StatedOrder(order)
}
