package core.payment

import java.util.Date

import com.lvxingpai.model.marketplace.order.{ Order, Prepay }
import core.api.OrderAPI
import core.exception.OrderStatusException
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
   * 获得一个Prepay对象. 如果对应的订单中已有Prepay, 则返回之; 否则创建一个.
   * @param orderId 订单号
   * @return
   */
  def getPrepay(orderId: Long): Future[(Prepay, Map[String, Any])] = {
    val providerName = provider.toString

    // 尝试从paymentInfo中获得Prepay, 否则就新建
    val result: Future[Option[(Prepay, Map[String, Any])]] =
      OrderAPI.getOrder(orderId, Seq("orderId", "totalPrice", "discount", "updateTime", "status", "expireDate",
        "commodity", "paymentInfo"))(datastore) flatMap (order => {

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
   * 获得订单在某个具体渠道的支付详情
   * @param orderId 订单号
   * @return
   */
  def getPaymentStatus(orderId: Long): Future[Boolean]
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
