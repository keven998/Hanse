package core.payment

import java.net.URL
import java.util.{ Date, UUID }
import javax.inject.Inject

import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.model.marketplace.order.{ Prepay, Order }
import core.misc.Utils
import core.payment.PaymentService.Provider
import org.mongodb.morphia.Datastore
import play.api.http.Writeable
import play.api.{ Configuration, Play }
import play.api.inject.BindingKey
import play.api.libs.ws.WS
import play.api.Play.current

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by zephyre on 12/17/15.
 */
class WeChatPaymentService @Inject() (private val morphiaMap: MorphiaMap) extends PaymentService {
  override def provider: Provider.Value = Provider.WeChat

  override def datastore: Datastore = morphiaMap.map("k2")

  /**
   * 计算签名
   * @param data
   * @return
   */
  private def genSign(data: Map[String, String]): String = {
    val stringA = (for ((k, v) <- data.toList.sorted) yield s"$k=$v").mkString("&")
    val stringSignTemp = stringA + "&key=" + WeChatPaymentService.apiSecrete
    Utils.MD5(stringSignTemp).toUpperCase
  }

  /**
   * 创建一个新的Prepay. 如果创建失败, 比如发生乐观锁冲突之类的情况, 则返回Future(None)
   *
   * @param order 订单
   * @return
   */
  override def createPrepay(order: Order): Future[Option[Prepay]] = {
    // 构造有待签名的Map
    val callBack = Map("notify_url" -> WeChatPaymentService.notifyUrl)
    val randomStr = Map("nonce_str" -> UUID.randomUUID().toString.replace("-", ""))

    val accountInfo = {
      Map("appid" -> WeChatPaymentService.appid, "mch_id" -> WeChatPaymentService.mchid)
    }
    val content = Map(
      "out_trade_no" -> order.orderId.toString,
      // WechatPrepay.FD_SPBILL_CREATE_IP -> ip,
      "body" -> order.commodity.title,
      "trade_type" -> "APP",
      "total_fee" -> (order.totalPrice.toDouble * 100).toInt.toString
    )
    val params: Map[String, String] = content ++ accountInfo ++ callBack ++ randomStr
    val sign = Map("sign" -> genSign(params))
    val resultParams = params ++ sign

    val body = Utils.addChildrenToXML(<xml></xml>, resultParams)

    for {
      response <- WS.url(WeChatPaymentService.unifiedOrderUrl)
        .withHeaders("Content-Type" -> "text/xml; charset=utf-8")
        .withRequestTimeout(30000)
        .post(body)
    } yield {
      val body = scala.xml.XML loadString (new String(response.bodyAsBytes, "UTF8"))
      val prepayId = (body \ "prepay_id").text
      val returnCode = (body \ "return_code").text
      val resultCode = (body \ "result_code").text
      val returnMsg = (body \ "return_msg").text
      val errorCode = (body \ "err_code").text
      val errorMsg = (body \ "err_code_des").text

      if (returnCode != "SUCCESS" || resultCode != "SUCCESS") {
        throw new RuntimeException(s"Error in creating WeChat prepay. return_msg=$returnMsg / " +
          s"err_code=$errorCode / err_code_des=$errorMsg")
      }

      val providerName = provider.toString

      // 创建新的Prepay对象
      val prepay = new Prepay
      prepay.provider = providerName
      prepay.amount = order.totalPrice - order.discount
      prepay.createTime = new Date
      prepay.updateTime = new Date
      prepay.prepayId = prepayId

      val query = datastore.createQuery(classOf[Order]) field "orderId" equal order.orderId field
        s"paymentInfo.$providerName" equal null
      val ops = datastore.createUpdateOperations(classOf[Order]).set(s"paymentInfo.$providerName", prepay)
      val updateResult = datastore.update(query, ops)
      if (updateResult.getUpdatedExisting) Some(prepay)
      else None
    }
  }

  /**
   * 获得订单在某个具体渠道的支付详情(即是否支付)
   * @param order 订单号
   * @return
   */
  override def refreshPaymentStatus(order: Order): Future[Order] = ???

  /**
   * 获得sidecar信息. (比如: 签名等, 就位于其中)
   * @return
   */
  override protected def createSidecar(order: Order, prepay: Prepay): Map[String, Any] = {
    val original = Map(
      "appid" -> WeChatPaymentService.appid,
      "partnerid" -> WeChatPaymentService.mchid,
      "prepayid" -> prepay.prepayId,
      "package" -> "Sign=WXPay",
      "noncestr" -> UUID.randomUUID().toString.replace("-", ""),
      "timestamp" -> (System.currentTimeMillis / 1000).toString
    )
    original + ("sign" -> genSign(original))
  }

  /**
   * 处理支付渠道服务器发来的异步调用
   * @param params
   * @return
   */
  override def handleCallback[C](params: Map[String, Any])(implicit wriable: Writeable[C]): C = ???
}

object WeChatPaymentService {
  lazy val instance = Play.application.injector.instanceOf[WeChatPaymentService]

  lazy private val conf = {
    val key = BindingKey(classOf[Configuration]) qualifiedWith "default"
    Play.current.injector instanceOf key
  }

  // 微信服务器的url
  lazy val unifiedOrderUrl = (conf getString "hanse.payment.wechat.unifiedOrderUrl").get

  lazy val notifyUrl = {
    val baseUrl = new URL(conf getString "hanse.baseUrl" getOrElse "http://localhost:9000")

    val protocol = baseUrl.getProtocol
    val host = baseUrl.getHost
    val port = Some(baseUrl.getPort) flatMap (p => if (p == -1 || p == 80) None else Some(p))
    val path1 = baseUrl.getPath
    val path2 = controllers.routes.PaymentCtrl.wechatCallback().url
    s"$protocol://$host${port map (p => s":$p") getOrElse ""}$path1$path2"
  }

  lazy val appid = (conf getString "hanse.payment.wechat.appid").get

  lazy val apiSecrete = (conf getString "hanse.payment.wechat.apisecret").get

  lazy val mchid = (conf getString "hanse.payment.wechat.mchid").get

}
