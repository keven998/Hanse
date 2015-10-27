package core.service

import com.typesafe.config.ConfigFactory
import core.misc.Utils
import play.api.Configuration
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.ws.WS

import scala.concurrent.Future

/**
 * Created by topy on 2015/10/22.
 */
object PaymentService {

  lazy val config = Configuration(ConfigFactory.load())
  val accountInfo = {
    val appid = config.getString("wechatpay.appid").get
    val mchid = config.getString("wechatpay.mchid").get
    Map("appid" -> appid, "mch_id" -> mchid)
  }

  val appsecret = config.getString("wechatpay.appsecret").get

  /**
   *
   * application/xml
   * @param url
   * @param contentType
   * @param body
   * @return
   */
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
    Future {
      val url = "https://api.mch.weixin.qq.com/pay/unifiedorder"
      val notify_url = "http://182.92.168.171:11219/app/payment-webhook/wechat"

      val callBack = Map("notify_url" -> notify_url)
      val randomStr = Map("nonce_str" -> Utils.nonceStr())

      val params = content ++ accountInfo ++ callBack ++ randomStr
      val sign = Map("sign" -> genSign(params))
      val resultParams = params ++ sign
      val body = Utils.addChildrenToXML(<xml></xml>, resultParams).toString()
      val ret = WS.url(url).withHeaders("Content-Type" -> "application/xml; charset=utf-8").post(body)
      ret
    }
  }

  def genSign(content: Map[String, String]) = {
    val stringA = (for ((k, v) <- content.toList.sorted) yield s"$k=$v").mkString("&")
    val stringSignTemp = stringA + "&key=" + appsecret
    val sign = Utils.MD5(stringSignTemp).toUpperCase()
    sign
  }

}
