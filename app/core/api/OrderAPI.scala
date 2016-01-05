package core.api

import com.lvxingpai.model.marketplace.order.{ Order, OrderActivity }
import core.model.trade.order.OrderStatus
import core.payment.{ AlipayService, PaymentService, WeChatPaymentService }
import org.joda.time.DateTime
import org.mongodb.morphia.Datastore
import org.mongodb.morphia.query.UpdateResults

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by topy on 2015/10/22.
 */
object OrderAPI {

  /**
   * 创建订单
   * @return
   */
  def createOrder(order: Order)(implicit ds: Datastore): Future[Order] = {
    Future {
      ds.save[Order](order)
      order
    }
  }

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
    act.action = "pay"
    act.timestamp = DateTime.now().toDate
    act.prevStatus = OrderStatus.Pending

    // 设置payment状态
    val paymentQuery = ds.createQuery(classOf[Order]) field "orderId" equal orderId field
      s"paymentInfo.$providerName" notEqual null
    val paymentOps = ds.createUpdateOperations(classOf[Order]).set(s"paymentInfo.$providerName.paid", true)

    // 如果订单还处于pending, 则将其设置为paid
    val statusQuery = ds.createQuery(classOf[Order]) field "orderId" equal orderId field
      s"paymentInfo.$providerName" notEqual null field "status" equal "pending"
    val statusOps = ds.createUpdateOperations(classOf[Order]).set("status", "paid").add("activities", act)

    Future.sequence(Seq(
      Future {
        ds.update(paymentQuery, paymentOps)
      }, Future {
        ds.update(statusQuery, statusOps)
      }
    )) map (_ => ())
  }

  /**
   * 将某个订单设置为取消
   *
   * @param orderId
   * @param data
   * @param ds
   * @return
   */
  def setCancel(orderId: Long, data: Map[String, Any] = Map())(implicit ds: Datastore): Future[UpdateResults] = {
    // 设置activity
    val act = new OrderActivity
    act.action = "cancel"
    act.timestamp = DateTime.now().toDate
    act.data = data.asJava
    act.prevStatus = OrderStatus.Pending
    Future {
      // 预支付订单，可关闭
      val query = ds.createQuery(classOf[Order]).field("orderId").equal(orderId).field("status").equal(OrderStatus.Pending)
      val updateOps = ds.createUpdateOperations(classOf[Order]).set("status", OrderStatus.Canceled).add("activities", act)
      val ret = ds.update(query, updateOps)
      ret
    }
  }

  /**
   * 将某个订单设置为申请退款
   *
   * @param orderId
   * @param data
   * @param ds
   * @return
   */
  def setRefundApplied(orderId: Long, data: Map[String, Any] = Map())(implicit ds: Datastore): Future[UpdateResults] = {
    // 设置activity
    val act = new OrderActivity
    act.action = "refund"
    act.timestamp = DateTime.now().toDate
    act.data = data.asJava
    act.prevStatus = OrderStatus.Paid
    Future {
      val query = ds.createQuery(classOf[Order]).field("orderId").equal(orderId)
      // 已支付或已发货的订单，可申请退款
      query.or(query.criteria("status").equal(OrderStatus.Paid), query.criteria("status").equal(OrderStatus.Committed))
      val updateOps = ds.createUpdateOperations(classOf[Order]).set("status", OrderStatus.RefundApplied).add("activities", act)
      ds.update(query, updateOps)
    }
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
  def updateOrderStatus(orderId: Long, status: String, act: OrderActivity)(implicit ds: Datastore): Future[UpdateResults] = {
    Future {
      val query = ds.createQuery(classOf[Order]).field("orderId").equal(orderId)
      val updateOps = ds.createUpdateOperations(classOf[Order]).set("status", status).add("activities", act)
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
      val query = ds.createQuery(classOf[Order]).field("consumerId").equal(userId).order("-id").offset(start).limit(count) //生成时间逆序
      if (status.nonEmpty) {
        val queryList = status.get.split(",").toSeq
        query.field("status").in(queryList)
      }
      query.asList()
    }
  }
}
