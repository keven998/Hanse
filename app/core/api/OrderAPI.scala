package core.api

import core.db.MorphiaFactory
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
  val service = "service"

  // 签约合作者身份ID
  val partner = "partner"

  // 签约卖家支付宝账号
  val seller = "seller"

  // 异步回调路径
  val asyncPath = "asyncPath"

  // 支付类型， 固定值
  val paymentType = "1"

  // 设置未付款交易的超时时间
  val itBPay = "itBPay"

  // 支付完，跳转到此Url
  val returnUrl = "returnUrl"

  // 签名
  val sign = "sign"

  def getOrder(orderId: ObjectId): Future[Unit] = {
    Future {
      ds.find(classOf[Order], Order.FD_COMMODITY, orderId).asList()
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
   * 根据订单信息生成订单字符串
   * @param orderInfo 订单信息
   * @return
   */
  def getOrderStr(orderInfo: Order): String = {

    // 签约合作者身份ID
    val partnerStr = "partner=" + "\"" + partner + "\""

    // 签约卖家支付宝账号
    val sellerStr = "&seller_id=" + "\"" + seller + "\""

    // 商户网站唯一订单号
    val orderIdStr = "&out_trade_no=" + "\"" + orderInfo.id + "\""

    // 商品名称
    val cmyNameStr = "&subject=" + "\"" + orderInfo.commodity.title + "\""

    // 商品详情
    val cmyDetailStr = "&body=" + "\"" + orderInfo.commodity.detail + "\""

    // 商品金额
    val totalFeeStr = "&total_fee=" + "\"" + orderInfo.totalPrice + "\""

    // 服务器异步通知页面路径
    val notifyUrlStr = "&notify_url=" + "\"" + asyncPath + "\""

    // 服务接口名称， 固定值
    val serviceStr = "&service=\"" + service + "\""

    // 支付类型， 固定值
    val paymentTypeStr = "&payment_type=\"" + paymentType + "\""

    // 参数编码， 固定值
    val inputCharset = "&_input_charset=\"utf-8\""

    // 设置未付款交易的超时时间
    // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
    // 取值范围：1m～15d。
    // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
    // 该参数数值不接受小数点，如1.5h，可转换为90m。
    val itBPayStr = "&it_b_pay=\"" + itBPay + "\""

    // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
    // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

    // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
    val returnUrlStr = "&return_url=\"" + returnUrl + "\""

    // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
    // orderInfo += "&paymethod=\"expressGateway\"";

    // 加密算法
    val signTypeStr = "&sign_type=\"RSA\""

    // 签名
    val signStr = "&sign=\"" + RSA.sign("", privateKey(), "utf-8") + "\""

    val orderInfoStr = partnerStr + sellerStr + orderIdStr + cmyNameStr + cmyDetailStr + totalFeeStr + notifyUrlStr + serviceStr +
      paymentTypeStr + inputCharset + itBPayStr + returnUrlStr + signStr

    orderInfoStr
  }

  /**
   * 取得私钥字符串
   * @return 私钥字符串
   */
  def privateKey(): String = {
    val src = scala.io.Source.fromFile("conf/rsa_private_key.pem").getLines()
    var result: String = ""
    while (src.hasNext) {
      result += src.next()
    }
    result
  }
}
