package controllers

import javax.inject._

import core.misc.HanseResult
import core.model.trade.order.Order
import core.model.trade.product.Commodity
import org.bson.types.ObjectId
import play.api.mvc.{ Action, Controller, Result }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import core.Implicits._

/**
 * Created by topy on 2015/10/22.
 */
@Singleton
class TradeCtrl extends Controller {

  /**
   * 根据订单商品Id取得商品信息
   * @param cmyId
   * @return
   */
  def getCommodityInfo(cmyId: String): Commodity = {
    val cmyInfo = new Commodity()
    cmyInfo
  }
  def createOrder(cmyId: String, qty: Int): Order = {
    val orderInfo = new Order()
    val cmyInfo = getCommodityInfo(cmyId)
    orderInfo.id = new ObjectId()
    orderInfo.commodity = cmyInfo
    orderInfo.totalPrice = cmyInfo.price * qty
    // TODO  save

    orderInfo
  }
  /**
   * create the order info. 创建订单信息
   *
   */
  def getOrderStr(cmyId: String, qty: Int): String = {

    val orderInfo = createOrder(cmyId: String, qty: Int)

    // 签约合作者身份ID
    val partnerStr = "partner=" + "\"" + TradeCtrl.partner + "\""

    // 签约卖家支付宝账号
    val sellerStr = "&seller_id=" + "\"" + TradeCtrl.seller + "\""

    // 商户网站唯一订单号
    val orderIdStr = "&out_trade_no=" + "\"" + orderInfo.id + "\""

    // 商品名称
    val cmyNameStr = "&subject=" + "\"" + orderInfo.commodity.title + "\""

    // 商品详情
    val cmyDetailStr = "&body=" + "\"" + orderInfo.commodity.detail + "\""

    // 商品金额
    val totalFeeStr = "&total_fee=" + "\"" + orderInfo.totalPrice + "\""

    // 服务器异步通知页面路径
    val notifyUrlStr = "&notify_url=" + "\"" + TradeCtrl.asyncPath + "\""

    // 服务接口名称， 固定值
    val serviceStr = "&service=\"" + TradeCtrl.service + "\""

    // 支付类型， 固定值
    val paymentTypeStr = "&payment_type=\"" + TradeCtrl.paymentType + "1\""

    // 参数编码， 固定值
    val inputCharset = "&_input_charset=\"utf-8\""

    // 设置未付款交易的超时时间
    // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
    // 取值范围：1m～15d。
    // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
    // 该参数数值不接受小数点，如1.5h，可转换为90m。
    val itBPayStr = "&it_b_pay=\"" + TradeCtrl.itBPay + "\""

    // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
    // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

    // 支付宝处理完请求后，当前页面跳转到商户指定页面的路径，可空
    val returnUrlStr = "&return_url=\"" + TradeCtrl.returnUrl + "\""

    // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
    // orderInfo += "&paymethod=\"expressGateway\"";

    // 加密算法
    val signTypeStr = "&sign_type=\"RSA\""

    // 签名
    val signStr = "&sign=\"" + TradeCtrl.sign + "\""

    val orderInfoStr = partnerStr + sellerStr + orderIdStr + cmyNameStr + cmyDetailStr + totalFeeStr + notifyUrlStr + serviceStr +
      paymentTypeStr + inputCharset + itBPayStr + returnUrlStr + signStr

    orderInfoStr
  }

  /**
   * 创建订单
   * @return 返回订单信息
   */
  def createOrder() = Action.async(
    request => {
      val orderInfo = for {
        body <- request.body.asJson
        cmyId <- (body \ "cmyId").asOpt[String]
        qty <- (body \ "qty").asOpt[Int]
      } yield {
        getOrderStr(cmyId, qty)
      }
      orderInfo getOrElse Future(HanseResult.unprocessable())
      null
    }
  )
}

object TradeCtrl {

  // 服务接口名称， 固定值
  val service = ""

  // 签约合作者身份ID
  val partner = ""

  // 签约卖家支付宝账号
  val seller = ""

  // 异步回调路径
  val asyncPath = ""

  // 支付类型， 固定值
  val paymentType = ""

  // 设置未付款交易的超时时间
  val itBPay = ""

  // 支付完，跳转到此Url
  val returnUrl = ""

  // 签名
  val sign = ""
}
