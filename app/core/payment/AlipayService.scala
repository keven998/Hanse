package core.payment

import java.util.{ UUID, Date }
import javax.inject.Inject

import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.model.marketplace.order.{ Prepay, Order }
import core.exception.ResourceNotFoundException
import core.payment.PaymentService.Provider
import org.mongodb.morphia.Datastore
import play.api.Play
import play.api.Play.current
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * 支付宝的相关接口
 *
 * Created by zephyre on 12/16/15.
 */
class AlipayService @Inject() (private val morphiaMap: MorphiaMap) extends PaymentService {

  override lazy val datastore: Datastore = morphiaMap.map("k2")

  override lazy val provider: Provider.Value = Provider.Alipay

  override def createPrepay(order: Order): Future[Option[Prepay]] = {
    val providerName = provider.toString

    // 创建新的Prepay对象
    val prepay = new Prepay
    prepay.provider = providerName
    prepay.amount = order.totalPrice - order.discount
    prepay.createTime = new Date
    prepay.updateTime = new Date
    prepay.prepayId = UUID.randomUUID().toString

    val query = datastore.createQuery(classOf[Order]) field "orderId" equal order.orderId field
      s"paymentInfo.$providerName" equal null
    Future {
      val ops = datastore.createUpdateOperations(classOf[Order]).set(s"paymentInfo.$providerName", prepay)
      val updateResult = datastore.update(query, ops)
      if (updateResult.getUpdatedExisting) Some(prepay)
      else None
    }
  }

  /**
   * 获得订单在某个具体渠道的支付详情
   * @param orderId 订单号
   * @return
   */
  override def getPaymentStatus(orderId: Long): Future[Boolean] = {
    val providerName = provider.toString

    val query = datastore.createQuery(classOf[Order]).field("orderId").equal(orderId)
      .retrievedFields(true, s"paymentInfo.$providerName")
    Future {
      val paid = Option(query.get) map (order => {
        mapAsScalaMap(order.paymentInfo) get providerName exists (_.paid)
      })
      paid getOrElse {
        // 如果paid为None, 说明query.get为null
        throw ResourceNotFoundException(s"Order #$orderId does not exist.")
      }
    }
  }
}

object AlipayService {
  lazy val instance = Play.application.injector.instanceOf[AlipayService]
}
