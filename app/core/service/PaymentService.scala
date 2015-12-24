package core.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.lvxingpai.model.marketplace.order.Prepay
import com.lvxingpai.model.marketplace.trade.PaymentVendor
import core.misc.Implicits._
import core.misc.{ HanseResult, Utils }
import core.model.trade.order.WechatPrepay
import play.api.Play.current
import play.api.libs.ws.WS

import scala.xml.XML

/**
 * Created by topy on 2015/10/22.
 */
object PaymentService {

  /**
   * 微信统一下单接口
   *
   */
  def unifiedOrder(content: Map[String, String]) = {
    val url = "https://api.mch.weixin.qq.com/pay/unifiedorder"
    val notify_url = "http://192.168.100.3:9480/marketplace/orders/payment-webhook/wechat"
    val callBack = Map("notify_url" -> notify_url)
    val randomStr = Map("nonce_str" -> Utils.nonceStr())

    val accountInfo = {
      //      val appid = config.getString("wechatpay.appid").getOrElse("")
      //      val mchid = config.getString("wechatpay.mchid").getOrElse("")
      val mchid = "1278401701"
      val appid = "wx86048e56adaf7486"
      Map("appid" -> appid, "mch_id" -> mchid)
    }
    //    val apisecret = config.getString("wechatpay.apisecret").getOrElse("")

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

    val accountInfo = {
      //      val appid = config.getString("wechatpay.appid").getOrElse("")
      //      val mchid = config.getString("wechatpay.mchid").getOrElse("")
      val mchid = "1278401701"
      val appid = "wx86048e56adaf7486"
      Map("appid" -> appid, "mch_id" -> mchid)
    }
    val params = content ++ accountInfo ++ randomStr
    val sign = Map("sign" -> genSign(params))
    val resultParams = params ++ sign
    val body = Utils.addChildrenToXML(<xml></xml>, resultParams)
    val ret = WS.url(url)
      .withHeaders("Content-Type" -> "text/xml; charset=utf-8")
      .withRequestTimeout(30000)
      .post(body.toString())
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
      //指定微信支付
      prepay.provider = PaymentVendor.Wechat
      //      prepay.id = new ObjectId()
      prepay.prepayId = (elem \ "prepay_id" \*).toString()
      //      prepay.setVendor(PaymentVendor.Wechat)
      //      prepay.setTradeType((elem \ WechatPrepay.FD_SIGN \*).toString())
      //prepay.setTimestamp(new Date())
      //      prepay.setSign((elem \ WechatPrepay.FD_SIGN \*).toString())
      //      prepay.setNonceString((elem \ WechatPrepay.FD_NONCE_STR \*).toString())
      //      prepay.setResult((elem \ WechatPrepay.FD_RETURN_MSG \*).toString())
      Some(prepay)
    }
  }

  def xml2OResult(body: String): ObjectNode = {
    val elem = XML.loadString(body)
    val returnCode = (elem \ WechatPrepay.FD_RETURN_CODE \*).toString()
    val resultCode = (elem \ WechatPrepay.FD_RESULT_CODE \*).toString()
    val node = new ObjectMapper().createObjectNode()
    if (returnCode.equals(WechatPrepay.VA_FAIL) ||
      resultCode.equals(WechatPrepay.VA_FAIL)) {
      node.put("result", (elem \ WechatPrepay.FD_RETURN_CODE \*).toString())
    } else {
      node.put("result", (elem \ WechatPrepay.FD_RETURN_CODE \*).toString())
      node.put("nonce_str", (elem \ WechatPrepay.FD_NONCE_STR \*).toString())
      node.put("prepay_id", (elem \ WechatPrepay.PREPAY_ID \*).toString())
      node.put("trade_type", (elem \ WechatPrepay.FD_TRADE_TYPE \*).toString())
      node.put("sign", (elem \ WechatPrepay.FD_SIGN \*).toString())
      node.put("mch_id", (elem \ WechatPrepay.MCH_ID \*).toString())

    }
  }

  /**
   * 取得签名
   *
   * @param content
   * @return
   */
  def genSign(content: Map[String, String]) = {
    val apisecret = "ba912c7748715e3c8e2cc1a6be751e86"
    val stringA = (for ((k, v) <- content.toList.sorted) yield s"$k=$v").mkString("&")
    val stringSignTemp = stringA + "&key=" + apisecret
    Utils.MD5(stringSignTemp).toUpperCase
  }

}
