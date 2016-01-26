package core.payment

import java.util.Date

import com.lvxingpai.model.marketplace.order.{ Order, Prepay }
import core.api.OrderAPI
import core.exception.{ OrderStatusException, ResourceNotFoundException }
import org.mongodb.morphia.Datastore

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * 支付接口
 *
 * Created by zephyre on 12/16/15.
 */
trait PaymentService {

  def provider: PaymentService.Provider.Value

  def datastore: Datastore

  /**
   * 创建一个新的Prepay. 如果创建失败, 比如发生乐观锁冲突之类的情况, 则返回Future(None)
   *
   * @param order 订单
   * @return
   */
  protected def createPrepay(order: Order): Future[Option[Prepay]]

  /**
   * 获得sidecar信息. (比如: 签名等, 就位于其中)
   * @return
   */
  protected def createSidecar(order: Order, prepay: Prepay): Map[String, Any]

  /**
   * 获得一个Prepay对象. 如果对应的订单中已有Prepay, 则返回之; 否则创建一个. 如果orderId错误, 将抛出ResourceNotFoundException异常
   * @param orderId 订单号
   * @return
   */
  def getPrepay(orderId: Long): Future[(Prepay, Map[String, Any])] = {
    val providerName = provider.toString

    // 尝试从paymentInfo中获得Prepay, 否则就新建
    val result: Future[Option[(Prepay, Map[String, Any])]] =
      OrderAPI.getOrder(orderId, Seq("orderId", "totalPrice", "discount", "updateTime", "status", "expireDate",
        "commodity", "paymentInfo"))(datastore) flatMap (opt => {
        val order = if (opt.nonEmpty) opt.get else throw ResourceNotFoundException(s"Invalid order id: $orderId")

        // 订单状态检查
        if (order.status != "pending")
          throw OrderStatusException(s"Order #$orderId status is ${order.status} instead of pending.")
        // 订单过期就不允许支付了
        if (order.expireDate before new Date())
          throw OrderStatusException(s"Order #$orderId is expired at ${order.expireDate.toString}")

        val paymentInfo = (Option(order.paymentInfo) map mapAsScalaMap) getOrElse mutable.Map()

        if (paymentInfo contains providerName) {
          // 获得prepay对象和相应的sidecar.
          val prepay: Prepay = paymentInfo(providerName)
          Future(Some(prepay -> createSidecar(order, prepay)))
        } else {
          // paymentInfo中没有相应的prepay, 新建一个
          createPrepay(order) map (opt => {
            opt map (prepay => prepay -> createSidecar(order, prepay))
          })
        }
      })

    result flatMap (v => {
      v map (Future(_)) getOrElse {
        // 乐观锁重试
        Thread sleep 200
        getPrepay(orderId)
      }
    })
  }

  /**
   * 获得订单在某个具体渠道的支付详情(即是否支付).
   * @param order 订单号
   * @return
   */
  def refreshPaymentStatus(order: Order): Future[Order]

  /**
   * 处理支付渠道服务器发来的异步调用
   * @param params
   * @return
   */
  def handleCallback(params: Map[String, Any]): Future[Any]

  /**
   * 执行退款操作
   * @param orderId
   * @param refundPrice
   * @return
   */
  def refund(userId: Long, orderId: Long, refundPrice: Option[Int], memo: String): Future[Unit] = {
    //    import Order.Status._
    //
    //    for {
    //      order <- OrderAPI.fetchOrder(orderId)(datastore)
    //      _ <- {
    //        assert(Seq(Paid, RefundApplied) contains (Order.Status withName order.status))
    //        assert(userId == order.commodity.seller.sellerId)
    //        val amount = refundPrice getOrElse (order.totalPrice - order.discount)
    //        if (amount > order.totalPrice)
    //          throw new IllegalArgumentException(s"Refund amount excesses the order's total price. ")
    //
    //        refundProcess(order, amount) map (_ => {
    //
    //        })
    //      }
    //    } OrderAPI.getOrder(orderId, Seq("orderId", "totalPrice", "paymentInfo", "status"))(datastore) flatMap (opt => {
    //      val order = opt.getOrElse(throw ResourceNotFoundException(s"Invalid order id: $orderId"))
    //
    //      // 商家可以对已支付的订单主动退款，也可以对申请的订单退款
    //      if (!(order.status equals Order.Status.RefundApplied.toString) && !(order.status equals Order.Status.Paid.toString))
    //        throw OrderStatusException(s"Not refund applied or paid order id: $orderId")
    //
    //      // 判断退款是否超额
    //      val totalPrice = order.totalPrice
    //      if (refundPrice.nonEmpty && refundPrice.get > totalPrice)
    //        throw ResourceNotFoundException(s"Refund price express. " +
    //          s"TotalPrice:$totalPrice,RefundPrice:$refundPrice,OrderId:$orderId")
    //
    //      // 判断是否有支付信息
    //      val payment = Option(order.paymentInfo) map (_.asScala.toMap) getOrElse {
    //        throw ResourceNotFoundException(s"Order not paid order id: $orderId")
    //      }
    //
    //      refundProcess(userId, order, refundPrice, memo) map (_ => {
    //        // 成功了, 修改OrderActivities
    //        // 描述订单退款流水
    //        val act = new OrderActivity()
    //        act.action = OrderActivity.Action.refundApprove.toString
    //        act.timestamp = new Date()
    //        val actData: Map[String, Any] = Map("userId" -> userId, "amount" -> refundPrice.get, "memo" -> memo)
    //        act.data = actData.asJava
    //        act.prevStatus = order.status
    //        OrderAPI.updateOrderStatus(order.orderId, Order.Status.Refunded, act)(datastore) map (_ =>
    //          order)
    //      })
    //    })
    null
  }

  /**
   * 执行和渠道相关的, 具体的退款操作
   * @return
   */
  def refundProcess(order: Order, amount: Int): Future[Unit]

  /**
   * 查询退款
   * @param params
   * @return
   */
  def refundQuery(params: Map[String, Any]): Future[Any]
}

object PaymentService {

  /**
   * 支付渠道
   */
  object Provider extends Enumeration {
    /**
     * 支付宝
     */
    val Alipay = Value("alipay")

    /**
     * 微信
     */
    val WeChat = Value("wechat")
  }

}
