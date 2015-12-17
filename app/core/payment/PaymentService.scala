package core.payment

import java.util.{ Date, UUID }

import com.lvxingpai.model.marketplace.order.{ Order, Prepay }
import core.api.OrderAPI
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
   * 获得一个Prepay对象. 如果对应的订单中已有Prepay, 则返回之; 否则创建一个.
   * @param orderId 订单号
   * @return
   */
  def getPrepay(orderId: Long): Future[Prepay] = {
    val providerName = provider.toString

    val result =
      for {
        order <- OrderAPI.getOrder(orderId, Seq("orderId", "totalPrice", "discount", "updateTime", "paymentInfo"))(datastore)
      } yield {
        mapAsScalaMap(order.paymentInfo) getOrElse (providerName, {
          // 创建新的Prepay对象
          val prepay = new Prepay
          prepay.provider = providerName
          prepay.amount = order.totalPrice - order.discount
          prepay.createTime = new Date
          prepay.updateTime = new Date
          prepay.prepayId = UUID.randomUUID().toString

          val query = datastore.createQuery(classOf[Order]) field "orderId" equal order.orderId field
            s"paymentInfo.$providerName" equal null
          val ops = datastore.createUpdateOperations(classOf[Order]).set(s"paymentInfo.$providerName", prepay)
          val updateResult = datastore.update(query, ops)
          if (updateResult.getUpdatedExisting) prepay
          else null
        })
      }

    result flatMap (r => if (r == null) getPrepay(orderId) else Future(r))
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
    val Wechat = Value("wechat")
  }
}
