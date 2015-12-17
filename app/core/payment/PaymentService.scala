package core.payment

import java.util.Date

import com.lvxingpai.model.marketplace.order.{ Order, Prepay }
import core.api.OrderAPI
import core.exception.OrderStatusException
import org.mongodb.morphia.Datastore

import scala.collection.JavaConversions._
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
  def createPrepay(order: Order): Future[Option[Prepay]]

  /**
   * 获得一个Prepay对象. 如果对应的订单中已有Prepay, 则返回之; 否则创建一个.
   * @param orderId 订单号
   * @return
   */
  def getPrepay(orderId: Long): Future[Prepay] = {
    val providerName = provider.toString

    // 尝试从paymentInfo中获得Prepay, 否则就新建
    val result = OrderAPI.getOrder(orderId, Seq("orderId", "totalPrice", "discount", "updateTime", "status",
      "expireDate", "paymentInfo"))(datastore) flatMap (order => {

      // 订单状态检查
      if (order.status != "pending")
        throw OrderStatusException(s"Order #$orderId status is ${order.status} instead of pending.")
      // 订单过期就不允许支付了
      if (order.expireDate before new Date())
        throw OrderStatusException(s"Order #$orderId is expired at ${order.expireDate.toString}")

      mapAsScalaMap(order.paymentInfo) get providerName map (o => Future(Some(o))) getOrElse createPrepay(order)
    })

    result flatMap (r => r map (Future(_)) getOrElse getPrepay(orderId))
  }
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
