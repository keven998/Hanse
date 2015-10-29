package core.service

import core.misc.{ Global, Utils }
import play.api.Play.current
import play.api.libs.ws.WS

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

  //  def sendPostRequest(url: String, contentType: String, body: String) = {
  //    val client = HttpClientBuilder.create().build()
  //    val post = new HttpPost(url)
  //    post.setHeader("Content-type", contentType)
  //    post.setEntity(new StringEntity(body))
  //    val response = client.execute(post).getEntity
  //    IOUtils.toString(response.getContent, response.getContentEncoding.getValue)
  //  }

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
      .post(body.toString)
    ret
  }

  def genSign(content: Map[String, String]) = {
    val stringA = (for ((k, v) <- content.toList.sorted) yield s"$k=$v").mkString("&")
    val stringSignTemp = stringA + "&key=" + apisecret
    val sign = Utils.MD5(stringSignTemp).toUpperCase
    sign
  }

}
