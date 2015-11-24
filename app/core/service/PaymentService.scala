package core.service

import com.lvxingpai.model.marketplace.order.Prepay
import com.lvxingpai.model.marketplace.trade.PaymentVendor
import core.misc.Implicits._
import core.misc.{ Global, HanseResult, Utils }
import core.model.trade.order.WechatPrepay
import org.bson.types.ObjectId
import play.api.Play.current
import play.api.libs.ws.WS

import scala.xml.XML

/**
 * Created by topy on 2015/10/22.
 */
object PaymentService {

  lazy val config = Global.conf
  val accountInfo = {
    val appid = config.getString("wechatpay.appid").getOrElse("")
    val mchid = config.getString("wechatpay.mchid").getOrElse("")
    Map("appid" -> appid, "mch_id" -> mchid)
  }

  val apisecret = config.getString("wechatpay.apisecret").getOrElse("")

  /**
   * 微信统一下单接口
   *
   */
  def unifiedOrder(content: Map[String, String]) = {
    val url = "https://api.mch.weixin.qq.com/pay/unifiedorder"
    val notify_url = "http://182.92.168.171:11219/payment-webhook/wechat"
    val callBack = Map("notify_url" -> notify_url)
    val randomStr = Map("nonce_str" -> Utils.nonceStr())

    val params = content ++ accountInfo ++ callBack ++ randomStr
    val sign = Map("sign" -> genSign(params))
    val resultParams = params ++ sign
    val body = Utils.addChildrenToXML(<xml></xml>, resultParams)
    val ret = WS.url(url)
      .withHeaders("Content-Type" -> "text/xml; charset=utf-8")
      .withRequestTimeout(30000)
      .post(body)
    ret
  }

  /**
   * 查询订单接口
   *
   * @param content
   * @return
   */
  def queryOrder(content: Map[String, String]) = {
    val url = "https://api.mch.weixin.qq.com/pay/orderquery"
    val randomStr = Map("nonce_str" -> Utils.nonceStr())

    val params = content ++ accountInfo ++ randomStr
    val sign = Map("sign" -> genSign(params))
    val resultParams = params ++ sign
    val body = Utils.addChildrenToXML(<xml></xml>, resultParams)
    val ret = WS.url(url)
      .withHeaders("Content-Type" -> "text/xml; charset=utf-8")
      .withRequestTimeout(30000)
      .post(body.toString)
    ret
  }

  def processResponse(body: String) = {
    val elem = XML.loadString(body)
    if ((elem \ WechatPrepay.FD_RETURN_CODE \*).toString().equals(WechatPrepay.VA_FAIL)) {
      val msg = (elem \ WechatPrepay.FD_RETURN_MSG \*).toString()
      HanseResult.unprocessableWithMsg(Some(msg))
    } else if ((elem \ WechatPrepay.FD_RESULT_CODE \*).toString().equals(WechatPrepay.VA_FAIL)) {
      val returnMsg = (elem \ WechatPrepay.FD_ERR_CODE_DES \*).toString()
      HanseResult.unprocessableWithMsg(Some(returnMsg))
    } else {
      HanseResult.unprocessableWithMsg(Some(("")))
    }
  }

  def xml2Obj(body: String) = {
    val elem = XML.loadString(body)
    val returnCode = (elem \ WechatPrepay.FD_RETURN_CODE \*).toString()
    val resultCode = (elem \ WechatPrepay.FD_RESULT_CODE \*).toString()
    if (returnCode.equals(WechatPrepay.VA_FAIL) ||
      resultCode.equals(WechatPrepay.VA_FAIL)) {
      None
    } else {
      val prepay = new Prepay()
      prepay.id = new ObjectId()
      prepay.prepayId = (elem \ "prepay_id" \*).toString()
      prepay.setVendor(PaymentVendor.Wechat)
      prepay.setTradeType((elem \ WechatPrepay.FD_SIGN \*).toString())
      //prepay.setTimestamp(new Date())
      prepay.setSign((elem \ WechatPrepay.FD_SIGN \*).toString())
      prepay.setNonceString((elem \ WechatPrepay.FD_NONCE_STR \*).toString())
      prepay.setResult((elem \ WechatPrepay.FD_RETURN_MSG \*).toString())
      Some(prepay)
    }
  }

  /**
   * 取得签名
   *
   * @param content
   * @return
   */
  def genSign(content: Map[String, String]) = {
    val stringA = (for ((k, v) <- content.toList.sorted) yield s"$k=$v").mkString("&")
    val stringSignTemp = stringA + "&key=" + apisecret
    Utils.MD5(stringSignTemp).toUpperCase
  }

}
