package core.api

import com.lvxingpai.model.marketplace.order.{ Order, OrderActivity }
import core.exception.ResourceNotFoundException
import core.formatter.marketplace.order.OrderFormatter
import core.payment.{ AlipayService, PaymentService, WeChatPaymentService }
import core.service.ViaeGateway
import org.joda.time.DateTime
import org.mongodb.morphia.query.UpdateResults
import org.mongodb.morphia.{ Datastore, Key }
import play.api.Play
import play.api.Play.current

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by topy on 2015/10/22.
 */
object OrderAPI {

  /**
   * 如果订单处于未支付的时候, 刷新订单的支付状态
   * @param order
   * @return
   */
  def refreshOrderPayment(order: Order): Future[Order] = {
    order.status match {
      case "pending" =>
        val paymentInfo = Option(order.paymentInfo) map mapAsScalaMap getOrElse scala.collection.mutable.Map()

        // 以尾递归的形式, 查看具体支付渠道的支付结果
        val itr = paymentInfo.iterator

        // 查看某个具体的渠道
        def refreshSinglePayment(): Future[Order] = {
          val entry = itr.next()
          val result: Future[Order] = entry._1 match {
            case s if s == PaymentService.Provider.Alipay.toString =>
              AlipayService.instance.refreshPaymentStatus(order)
            case s if s == PaymentService.Provider.WeChat.toString =>
              WeChatPaymentService.instance.refreshPaymentStatus(order)
            case _ => Future(order) // 如果既不是微信, 也不是支付宝, 直接返回自身
          }

          result flatMap (order => {
            if (order.status == "pending") {
              // 依然处于待支付的状态, 尝试刷新下一个渠道
              if (itr.hasNext) refreshSinglePayment()
              else Future(order)
            } else {
              // 已经不再是待支付状态了
              Future(order)
            }
          })
        }
        if (itr.hasNext) refreshSinglePayment()
        else Future(order)
      case _ =>
        Future(order)
    }
  }

  /**
   * 将某个订单设置为已支付
   *
   * @param orderId 订单号
   * @param provider 支付渠道
   */
  def setPaid(orderId: Long, provider: PaymentService.Provider.Value)(implicit ds: Datastore): Future[Unit] = {
    val providerName = provider.toString
    // 设置activity
    val act = new OrderActivity
    act.action = OrderActivity.Action.pay.toString
    act.timestamp = DateTime.now().toDate
    act.prevStatus = Order.Status.Pending.toString

    // 设置payment状态
    val paymentQuery = ds.createQuery(classOf[Order]) field "orderId" equal orderId field
      s"paymentInfo.$providerName" notEqual null
    val paymentOps = ds.createUpdateOperations(classOf[Order]).set(s"paymentInfo.$providerName.paid", true)

    // 如果订单还处于pending, 则将其设置为paid, 同时更新订单的expireDate
    val statusQuery = ds.createQuery(classOf[Order]) field "orderId" equal orderId field
      s"paymentInfo.$providerName" notEqual null field "status" equal "pending"
    val statusOps = ds.createUpdateOperations(classOf[Order]).set("status", "paid").add("activities", act)
      .set("expireDate", DateTime.now().plusDays(1).toDate)

    val ret: Future[Seq[UpdateResults]] = Future.sequence(Seq(
      Future {
        ds.update(paymentQuery, paymentOps)
      }, Future {
        val updateResult = ds.update(statusQuery, statusOps)
        if (updateResult.getUpdatedCount == 1) {
          // 真正的从pending到paid, 触发onPayOrder事件
          getOrder(orderId) flatMap (value => {
            val order = value.get
            val viae = Play.application.injector instanceOf classOf[ViaeGateway]
            val orderNode = OrderFormatter.instance.formatJsonNode(order)
            viae.sendTask("viae.event.marketplace.onPayOrder", kwargs = Some(Map("order" -> orderNode)))
          })
        }
        updateResult
      }
    ))

    ret map (_ => ())
  }

  /**
   * 将某个订单设置为取消
   *
   * @param orderId
   * @param data
   * @param ds
   * @return
   */
  def setCancel(orderId: Long, data: Map[String, Any] = Map())(implicit ds: Datastore): Future[Key[Order]] = {
    // 设置activity
    Future {
      Option(ds.createQuery(classOf[Order]).field("orderId").equal(orderId).field("status").equal(Order.Status.Pending.toString).get())
    } map (orderOpt => {
      if (orderOpt.isEmpty)
        throw ResourceNotFoundException(s"Pending order not exists.OrderId:$orderId")
      val order = orderOpt.get
      val act = new OrderActivity
      act.action = OrderActivity.Action.cancel.toString
      act.timestamp = DateTime.now().toDate
      act.data = data.asJava
      act.prevStatus = order.status

      order.activities = order.activities += act
      order.status = Order.Status.Canceled.toString
      ds.save[Order](order)
    })
  }

  /**
   * 将某个订单设置为申请退款
   *
   * @param orderId
   * @param data
   * @param ds
   * @return
   */
  def setRefundApplied(orderId: Long, data: Map[String, Any] = Map())(implicit ds: Datastore): Future[Unit] = {

    val ret = Future {
      val query = ds.createQuery(classOf[Order]).field("orderId").equal(orderId)
      // 已支付或已发货的订单，可申请退款
      query.or(query criteria "status" equal Order.Status.Paid.toString, query criteria "status" equal
        Order.Status.Committed.toString)
      Option(query.get())
    } map (orderOpt => {
      if (orderOpt.isEmpty)
        throw ResourceNotFoundException(s"Committed or Paid order not exists.OrderId:$orderId")
      val order = orderOpt.get

      // 设置activity
      val act = new OrderActivity
      act.action = OrderActivity.Action.refundApply.toString
      act.timestamp = DateTime.now().toDate
      act.data = data.asJava
      act.prevStatus = order.status

      // Order赋值
      order.activities = order.activities += act
      order.status = Order.Status.RefundApplied.toString
      ds.save[Order](order)
      order
    })
    // 注册退款申请任务
    ret flatMap (order => {
      Future {
        val viae = Play.application.injector instanceOf classOf[ViaeGateway]
        val orderNode = OrderFormatter.instance.formatJsonNode(order)
        // 申请退款的理由和备注
        val reason = data.getOrElse("reason", "")
        val memo = data.getOrElse("memo", "")
        viae.sendTask("viae.event.marketplace.onRefundApply", kwargs = Some(Map(
          "order" -> orderNode, "reason" -> reason.toString.trim(), "memo" -> memo.toString.trim()
        )))
      }
    })
  }

  def setExpire(orderId: Long, data: Map[String, Any] = Map())(implicit ds: Datastore): Future[Key[Order]] = {
    Future {
      Option(ds.find(classOf[Order], "orderId", orderId)).get
    } map (orderOpt => {
      if (orderOpt.isEmpty)
        throw ResourceNotFoundException(s"To be expired order not exists.OrderId:$orderId")
      val order = orderOpt.get()

      // 设置activity
      val act = new OrderActivity
      act.action = OrderActivity.Action.expire.toString
      act.timestamp = DateTime.now().toDate
      act.data = data.asJava
      act.prevStatus = order.status

      // Order赋值
      order.activities = order.activities += act
      order.status match {
        case p if p == Order.Status.Pending.toString => order.status = Order.Status.Canceled.toString
        case s if s == Order.Status.Paid.toString | s == Order.Status.RefundApplied.toString =>
          order.status = Order.Status.Refunded.toString
          WeChatPaymentService.instance.refund(0, orderId, Some(order.totalPrice), "")
      }
      ds.save[Order](order)
    })
  }

  def setFinish(orderId: Long, data: Map[String, Any] = Map())(implicit ds: Datastore): Future[Key[Order]] = {
    Future {
      // 只有商家确认发货的订单（commit状态），才能设为finish
      Option(ds.createQuery(classOf[Order]).field("orderId").equal(orderId).field("status").equal(Order.Status.Committed.toString)).get
    } map (orderOpt => {
      if (orderOpt.isEmpty)
        throw ResourceNotFoundException(s"Committed order not exists.OrderId:$orderId")
      val order = orderOpt.get()
      // 设置activity
      val act = new OrderActivity
      act.action = OrderActivity.Action.finish.toString
      act.timestamp = DateTime.now().toDate
      act.data = data.asJava
      act.prevStatus = order.status
      // Order赋值
      order.activities = order.activities += act
      order.status = Order.Status.Finished.toString
      ds.save[Order](order)
    })
  }

  /**
   * 根据订单id查询订单信息
   * @param orderId 订单id
   * @return 订单信息
   */
  def getOrder(orderId: Long)(implicit ds: Datastore): Future[Option[Order]] = {
    Future {
      Option(ds.find(classOf[Order], "orderId", orderId).get)
    } flatMap (orderOpt => {
      if (orderOpt.isEmpty) {
        Future(None)
      } else {
        val order = orderOpt.get
        refreshOrderPayment(order) map Option.apply
      }
    })
  }

  /**
   * 根据订单id查询订单信息
   * @param orderId 订单id
   * @param fields 查询哪些字段
   * @return
   */
  def getOrder(orderId: Long, fields: Seq[String])(implicit ds: Datastore): Future[Option[Order]] = {
    Future {
      Option(ds.find(classOf[Order], "orderId", orderId).retrievedFields(true, fields: _*).get)
    } flatMap (orderOpt => {
      if (orderOpt.isEmpty) {
        Future(None)
      } else {
        val order = orderOpt.get
        refreshOrderPayment(order) map Option.apply
      }
    })
  }

  /**
   *
   * @param orderId
   * @param ds
   * @return
   */
  def getOrderOnlyStatus(orderId: Long)(implicit ds: Datastore): Future[Order] = {
    Future {
      ds.find(classOf[Order], "orderId", orderId).retrievedFields(true, Seq("consumerId", "status"): _*).get
    }
  }

  /**
   * 更新订单状态
   * @param orderId 订单号
   * @param status 订单状态
   */
  def updateOrderStatus(orderId: Long, status: Order.Status.Value, act: OrderActivity)(implicit ds: Datastore): Future[UpdateResults] = {
    Future {
      val query = ds.createQuery(classOf[Order]).field("orderId").equal(orderId)
      val updateOps = ds.createUpdateOperations(classOf[Order]).set("status", status.toString).add("activities", act)
      ds.update(query, updateOps)
    }
  }

  /**
   * 根据用户id获取订单列表
   * 如果订单状态为空, 获取所在用户下的所有的订单列表
   * 如果订单状态不为空, 获取所在用户下的某个订单状态的订单列表
   * @param userId 用户id
   * @param status 订单状态
   * @return 订单列表
   */
  def getOrderList(userId: Long, status: Option[String], start: Int, count: Int)(implicit ds: Datastore): Future[Seq[Order]] = {
    Future {
      val query = ds.createQuery(classOf[Order]).field("consumerId").equal(userId).order("createTime") //生成时间逆序
      if (status.nonEmpty) {
        val queryList = status.get.split(",").toSeq
        query.field("status").in(queryList)
      }
      // 按照生成时间逆序排列且分页，为避免-createTime时的bug
      query.asList().reverse.subList(start, start + count)
    }
  }
}
