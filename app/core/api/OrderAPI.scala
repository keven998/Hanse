package core.api

import core.db.MorphiaFactory
import core.misc.Global
import core.model.trade.order.Order
import core.sign.RSA

import scala.concurrent.Future
import org.bson.types.ObjectId
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Created by topy on 2015/10/22.
 */
object OrderAPI {

  val ds = MorphiaFactory.datastore
  // 服务接口名称， 固定值
  val aliService = "mobile.securitypay.pay"

  // 签约合作者身份ID
  val aliPartner = Global.conf.getString("alipayAPI.partner").get

  // 签约卖家支付宝账号
  val aliSeller = Global.conf.getString("alipayAPI.seller").get

  // 异步回调路径
  val aliNotifyUrl = Global.conf.getString("alipayAPI.notifyUrl").get

  // 支付类型， 固定值
  val aliPaymentType = "1"

  // 设置未付款交易的超时时间
  val aliItBPay = "1440m"

  /**
   * 根据订单id查询订单信息
   * @param orderId 订单id
   * @return 订单信息
   */
  def getOrder(orderId: ObjectId): Future[Order] = {
    Future {
      ds.find(classOf[Order], Order.FD_ID, orderId).get
    }
  }

  /**
   * 创建订单
   * @param cmyId 商品Id
   * @param qty 商品数量
   * @return
   */
  def addOrder(cmyId: String, qty: Int): Future[Order] = {
    val futureCmy = CommodityAPI.getCommodityById(cmyId)
    for {
      cmy <- futureCmy
    } yield {
      val order = Order(cmy, qty)
      ds.save[Order](order)
      order
    }
  }

  /**
   * 根据订单信息生成支付宝所需信息的字符串
   * @param orderInfo 订单信息
   * @return 支付宝支付所需信息
   */
  def getAlipayPrePayStr(orderInfo: Order): String = {

    // 签约合作者身份ID
    val partnerStr = "partner=" + "\"" + aliPartner + "\""

    // 签约卖家支付宝账号
    val sellerStr = "&seller_id=" + "\"" + aliSeller + "\""

    // 商户网站唯一订单号
    val orderIdStr = "&out_trade_no=" + "\"" + orderInfo.id + "\""

    // 商品名称
    val cmyNameStr = "&subject=" + "\"" + orderInfo.commodity.title + "\""

    // 商品详情
    val cmyDetailStr = "&body=" + "\"" + orderInfo.commodity.detail + "\""

    // 商品金额
    val totalFeeStr = "&total_fee=" + "\"" + orderInfo.totalPrice + "\""

    // 服务器异步通知页面路径
    val notifyUrlStr = "&notify_url=" + "\"" + aliNotifyUrl + "\""

    // 服务接口名称， 固定值
    val serviceStr = "&service=\"" + aliService + "\""

    // 支付类型， 固定值
    val paymentTypeStr = "&payment_type=\"" + aliPaymentType + "\""

    // 参数编码， 固定值
    val inputCharset = "&_input_charset=\"utf-8\""

    // 设置未付款交易的超时时间
    // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
    // 取值范围：1m～15d。
    // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
    // 该参数数值不接受小数点，如1.5h，可转换为90m。
    val itBPayStr = "&it_b_pay=\"" + aliItBPay + "\""

    // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
    // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

    // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
    // orderInfo += "&paymethod=\"expressGateway\"";

    val orderInfoStr = partnerStr + sellerStr + orderIdStr + cmyNameStr + cmyDetailStr + totalFeeStr + notifyUrlStr + serviceStr +
      paymentTypeStr + inputCharset + itBPayStr

    // 签名
    val signStr = "&sign=\"" + RSA.sign(orderInfoStr, privateKey(), "utf-8") + "\""

    // 加密算法
    val signTypeStr = "&sign_type=\"RSA\""

    val result = orderInfoStr + signStr + signTypeStr

    result
  }

  /**
   * 根据订单信息生成微信所需信息的字符串
   * @param orderInfo 订单信息
   * @return 微信支付所需信息
   */
  def getWeixinPrePayStr(orderInfo: Order): String = {
    ""
  }

  /**
   * 根据订单信息生成银联所需信息的字符串
   * @param orderInfo 订单信息
   * @return 银联支付所需信息
   */
  def getUnionPrePayStr(orderInfo: Order): String = {
    ""
  }

  /**
   * 取得私钥字符串
   * @return 私钥字符串
   */
  def privateKey(): String = {
    scala.io.Source.fromFile("conf/rsa_private_key.pem").getLines().mkString("")
  }

  /**
   * 预支付
   * @param payMerchant 支付商
   * @param orderId 订单ID
   * @return 预支付信息
   */
  def prePay(payMerchant: String, orderId: String): Future[String] = {
    // 取得订单信息
    val futureOrderInfo = getOrder(new ObjectId(orderId))

    // 根据不同的支付商家, 返回不同的串
    payMerchant match {
      case "alipay" => futureOrderInfo map (orderInfo => getAlipayPrePayStr(orderInfo))
      case "weixin" => futureOrderInfo map (orderInfo => getWeixinPrePayStr(orderInfo))
      case "union" => futureOrderInfo map (orderInfo => getUnionPrePayStr(orderInfo))
      case _ => throw new IllegalArgumentException
    }
  }
}
