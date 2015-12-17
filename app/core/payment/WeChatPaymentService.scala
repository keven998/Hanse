package core.payment

import javax.inject.Inject

import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.model.marketplace.order.{ Prepay, Order }
import core.payment.PaymentService.Provider
import org.mongodb.morphia.Datastore

import scala.concurrent.Future

/**
 * Created by zephyre on 12/17/15.
 */
class WeChatPaymentService @Inject() (private val morphiaMap: MorphiaMap) extends PaymentService {
  override def provider: Provider.Value = Provider.WeChat

  override def datastore: Datastore = morphiaMap.map("k2")

  /**
   * 创建一个新的Prepay. 如果创建失败, 比如发生乐观锁冲突之类的情况, 则返回Future(None)
   *
   * @param order 订单
   * @return
   */
  override def createPrepay(order: Order): Future[Option[Prepay]] = ???

  /**
   * 获得订单在某个具体渠道的支付详情
   * @param orderId 订单号
   * @return
   */
  override def getPaymentStatus(orderId: Long): Future[Boolean] = ???

  /**
   * 获得sidecar信息. (比如: 签名等, 就位于其中)
   * @return
   */
  override protected def createSidecar(order: Order, prepay: Prepay): Map[String, Any] = ???
}
