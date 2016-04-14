package core.payment

import com.lvxingpai.model.marketplace.order.{ Bounty, Prepay }
import core.api.BountyAPI
import core.exception.ResourceNotFoundException
import core.payment.PaymentService.Provider
import org.mongodb.morphia.Datastore

import scala.collection.JavaConversions._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Bounty支付接口
 *
 * Created by topy on 04/11/16.
 */
trait BountyPay {

  def provider: Provider.Value

  def datastore: Datastore

  /**
   * 创建一个新的Prepay. 如果创建失败, 比如发生乐观锁冲突之类的情况, 则返回Future(None)
   *
   * @param payment 支付对象
   * @return
   */
  protected def createPrepay(payment: Bounty): Future[Option[Prepay]]

  /**
   * 获得sidecar信息. (比如: 签名等, 就位于其中)
   * @return
   */
  protected def createSidecar(payment: Bounty, prepay: Prepay): Map[String, Any]

  /**
   * 获得一个Prepay对象. 如果对应的订单中已有Prepay, 则返回之; 否则创建一个. 如果orderId错误, 将抛出ResourceNotFoundException异常
   * @param paymentId 订单号
   * @return
   */
  def getPrepay(paymentId: Long): Future[(Prepay, Map[String, Any])] = {
    val providerName = provider.toString

    // 尝试从paymentInfo中获得Prepay, 否则就新建
    val result: Future[Option[(Prepay, Map[String, Any])]] =
      BountyAPI.getBounty(paymentId, Seq("itemId", "bountyPrice", "bountyPaid", "schedulePaid", "status", "scheduled",
        "paymentInfo"))(datastore) flatMap (opt => {
        val bounty = if (opt.nonEmpty) opt.get else throw ResourceNotFoundException(s"Invalid bounty id: $paymentId")

        val paymentInfo = (Option(bounty.paymentInfo) map mapAsScalaMap) getOrElse mutable.Map()

        if (paymentInfo contains providerName) {
          // 获得prepay对象和相应的sidecar.
          val prepay: Prepay = paymentInfo(providerName)
          Future(Some(prepay -> createSidecar(bounty, prepay)))
        } else {
          // paymentInfo中没有相应的prepay, 新建一个
          createPrepay(bounty) map (opt => {
            opt map (prepay => prepay -> createSidecar(bounty, prepay))
          })
        }
      })

    result flatMap (v => {
      v map (Future(_)) getOrElse {
        // 乐观锁重试
        Thread sleep 200
        getPrepay(paymentId)
      }
    })
  }

  /**
   * 获得订单在某个具体渠道的支付详情(即是否支付).
   * @param order 订单号
   * @return
   */
  def refreshPaymentStatus(order: Bounty): Future[Bounty]

  /**
   * 处理支付渠道服务器发来的异步调用
   * @param params
   * @return
   */
  def handleCallback(params: Map[String, Any]): Future[Any]

  /**
   * 执行退款操作
   * @param bountyId
   * @param refundPrice
   * @return
   */
  def refund(userId: Long, bountyId: Long, refundPrice: Option[Int], memo: String): Future[Unit] = ???

  /**
   * 执行和渠道相关的, 具体的退款操作
   * @return
   */
  def refundProcess(bounty: Bounty, amount: Int): Future[Unit]

  /**
   * 查询退款
   * @param params
   * @return
   */
  def refundQuery(params: Map[String, Any]): Future[Any]
}

object BountyPay {

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
