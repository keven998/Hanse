package core.payment

import java.net.{ URL, URLEncoder }
import java.util.Date
import javax.inject.Inject

import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.model.marketplace.order.{ Bounty, Prepay }
import core.api.BountyAPI
import core.exception.GeneralPaymentException
import core.formatter.marketplace.order.BountyFormatter
import core.misc.Utils
import core.payment.PaymentService.Provider
import core.service.ViaeGateway
import org.mongodb.morphia.Datastore
import play.api.Play.current
import play.api.inject.BindingKey
import play.api.{ Configuration, Play }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by topy on 2016/4/11.
 */
class BountyPayAli @Inject() (private val morphiaMap: MorphiaMap, implicit private val viaeGateway: ViaeGateway) extends BountyPay {

  override lazy val datastore: Datastore = morphiaMap.map("k2")

  override lazy val provider: Provider.Value = Provider.Alipay

  override def createPrepay(bounty: Bounty): Future[Option[Prepay]] = {
    val providerName = provider.toString

    // 创建新的Prepay对象
    val prepay = new Prepay
    prepay.provider = providerName
    prepay.amount = bounty.bountyPrice
    prepay.createTime = new Date
    prepay.updateTime = new Date
    prepay.prepayId = bounty.itemId.toString

    val query = datastore.createQuery(classOf[Bounty]) field "itemId" equal bounty.itemId field
      s"paymentInfo.$providerName" equal null
    Future {
      val ops = datastore.createUpdateOperations(classOf[Bounty]).set(s"paymentInfo.$providerName", prepay)
      val updateResult = datastore.update(query, ops)
      if (updateResult.getUpdatedExisting) Some(prepay)
      else None
    }
  }

  /**
   * 获得订单在某个具体渠道的支付详情(即是否支付). 由于支付宝不提供主动查询接口, 所以直接返回paymentInfo中的值
   * @param bounty 订单号
   * @return
   */
  override def refreshPaymentStatus(bounty: Bounty): Future[Bounty] = Future(bounty)

  /**
   * 获得sidecar信息. (比如: 签名等, 就位于其中)
   * @return
   */
  override protected def createSidecar(bounty: Bounty, prepay: Prepay): Map[String, Any] = {
    // 返回带有签名的请求字符串
    val requestMap = BountyPayAli.RequestMap(prepay.prepayId, bounty.consumerId.toString, bounty.itemId.toString + bounty.bountyPrice,
      bounty.bountyPrice)
    Map("requestString" -> requestMap.requestString)
  }

  /**
   * 处理支付渠道服务器发来的异步调用
   * @param params
   * @return
   */
  override def handleCallback(params: Map[String, Any]): Future[Any] = {
    // 将Map[String, Seq[String]]转换成Map[String, String]
    val data = params mapValues {
      case ss: Seq[_] => ss mkString ""
      case s: String => s
    }

    implicit val ds = datastore

    try {
      // 检查签名是否正常
      if (!BountyPayAli.verifyAlipay(data, data("sign")))
        throw GeneralPaymentException("Alipay signature check failed.")

      // 检查交易状态
      val tradeStatus = data.getOrElse("trade_status", "")
      tradeStatus match {
        case "WAIT_BUYER_PAY" | "TRADE_CLOSED" => Future("success") // 忽略该请求
        case "TRADE_SUCCESS" | "TRADE_FINISHED" =>
          // 订单支付成功
          // 获得订单状态
          val tradeNumber = data.getOrElse("out_trade_no", "")
          val bountyId = try {
            tradeNumber.toLong
          } catch {
            case _: NumberFormatException => throw GeneralPaymentException(s"Invalid out_trade_no: $tradeNumber")
          }

          for {

            _ <- BountyAPI.setBountyPaid(bountyId, PaymentService.Provider.Alipay)
          } yield {
            "success"
          }
      }
    } catch {
      case e: GeneralPaymentException => Future {
        throw e
      }
    }
  }

  /**
   * 查询退款
   * @param params
   * @return
   */
  override def refundQuery(params: Map[String, Any]): Future[Any] = ???

  /**
   * 执行退款操作
   * @param orderId
   * @param refundPrice
   * @return
   */
  override def refund(userId: Long, orderId: Long, refundPrice: Option[Int], memo: String): Future[Unit] = null

  /**
   * 计算签名
   * @param data
   * @return
   */
  private def genSign(data: Map[String, String]): String = {
    val stringA = (for ((k, v) <- data.toList.sorted) yield s"$k=$v").mkString("&")
    val stringSignTemp = stringA + "&key=" + BountyPayAli.md5Key
    Utils.MD5(stringSignTemp).toUpperCase
  }

  /**
   * 退款操作
   * @return
   */
  override def refundProcess(bounty: Bounty, amount: Int): Future[Unit] = Future {
    val viae = Play.application.injector instanceOf classOf[ViaeGateway]
    val orderNode = BountyFormatter.instance.formatJsonNode(bounty)
    // viae.sendTask("viae.job.marketplace.alipayRefund", kwargs = Some(Map("order" -> orderNode, "amount" -> amount)))
  }
}

object BountyPayAli {
  lazy val instance = Play.application.injector.instanceOf[BountyPayAli]

  lazy private val conf = {
    val key = BindingKey(classOf[Configuration]) qualifiedWith "default"
    Play.current.injector instanceOf key
  }

  lazy private val partner = (conf getString "hanse.payment.alipay.partner").get

  lazy private val sellerId = (conf getString "hanse.payment.alipay.id").get
  lazy private val refundOrderUrl = (conf getString "hanse.payment.alipay.refundOrderUrl").get
  lazy private val md5Key = (conf getString "hanse.payment.alipay.md5Key").get

  lazy private val notifyUrl = {
    val baseUrl = new URL(conf getString "hanse.baseUrl" getOrElse "http://localhost:9000")

    val protocol = baseUrl.getProtocol
    val host = baseUrl.getHost
    val port = Some(baseUrl.getPort) flatMap (p => if (p == -1 || p == 80) None else Some(p))
    val path1 = baseUrl.getPath
    val path2 = controllers.routes.BountyCtrl.alipayCallback("bounties").url
    s"$protocol://$host${port map (p => s":$p") getOrElse ""}$path1$path2"
  }

  /**
   * 取得私钥字符串
   * @return 私钥字符串
   */
  lazy private val privateKey = (conf getString "hanse.payment.alipay.privateKey").get

  lazy val alipayPublicKey = (conf getString "hanse.payment.alipay.alipayPublicKey").get

  /**
   * 验证支付宝签名
   * @param params 签名所需的数据
   * @param sign 签名
   * @return 签名是否通过
   */
  def verifyAlipay(params: Map[String, String], sign: String): Boolean = {
    // 将获取的数据按字典排序
    val sortedKeys = params.keys.toSeq.sorted
    // 剔除"sign", "sign_type"字段, 将数据组装成所需的字符串
    val contents = sortedKeys filterNot (Seq("sign", "sign_type") contains _) map
      (key => s"$key=${params(key)}") mkString "&"
    RSA.verify(contents, sign, alipayPublicKey, "utf-8")
  }

  /**
   * 支付宝调用请求
   *
   * @param outTradeNo 交易订单号
   * @param subject 商品标题
   * @param body 商品介绍
   * @param amount 订单金额
   */
  case class RequestMap(outTradeNo: String, subject: String, body: String, amount: Float) {
    val service = "mobile.securitypay.pay"
    val charset = "utf-8"
    val signType = "RSA"
    val paymentType = "1"

    lazy val requestString = {
      val requestMap = Map("service" -> service, "partner" -> partner, "_input_charset" -> charset,
        "seller_id" -> sellerId, "total_fee" -> (amount / 100.0).toString,
        "notify_url" -> notifyUrl, "out_trade_no" -> (outTradeNo take 64), "payment_type" -> paymentType,
        "subject" -> (subject take 128).replaceAllLiterally("\"", ""),
        "body" -> (body take 512).replaceAllLiterally("\"", ""))

      val parameters: Seq[String] = (requestMap map (entry => {
        val key = entry._1
        val value = entry._2
        s"""$key=\"$value\""""
      })).toSeq

      // 签名
      val sign = URLEncoder.encode(RSA.sign(parameters mkString "&", privateKey, "utf-8"), "utf-8")
      parameters ++ Seq(s"""sign=\"$sign\"""", s"""sign_type=\"$signType\"""") mkString "&"
    }
  }

}