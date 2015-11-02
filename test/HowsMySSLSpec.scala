
import java.io.File

import com.typesafe.config.{ ConfigFactory, ConfigValueFactory }
import play.api.libs.json.JsSuccess
import play.api.libs.ws._
import play.api.libs.ws.ning._
import play.api.test._
import play.api.{ Environment, Mode }

import scala.collection.JavaConverters._

/**
 * Created by topy on 2015/10/29.
 */
class HowsMySSLSpec extends PlaySpecification {

  def createClient(rawConfig: play.api.Configuration): WSClient = {
    val classLoader = Thread.currentThread().getContextClassLoader
    val parser = new WSConfigParser(rawConfig, new Environment(new File("."), classLoader, Mode.Test))
    val clientConfig = new NingWSClientConfig(parser.parse())
    // Debug flags only take effect in JSSE when DebugConfiguration().configure is called.
    //import play.api.libs.ws.ssl.debug.DebugConfiguration
    //clientConfig.ssl.map {
    //   _.debug.map(new DebugConfiguration().configure)
    //}
    val builder = new NingAsyncHttpClientConfigBuilder(clientConfig)
    val client = new NingWSClient(builder.build())
    client
  }

  def configToMap(configString: String): Map[String, _] = {
    ConfigFactory.parseString(configString).root().unwrapped().asScala.toMap
  }

  "WS" should {

    "verify common behavior" in {
      // GeoTrust SSL CA - G2 intermediate certificate not found in cert chain!
      // See https://github.com/jmhodges/howsmyssl/issues/38 for details.
      val geoTrustPem =
        """-----BEGIN CERTIFICATE-----
          |MIIEYjCCA8ugAwIBAgIDB8q7MA0GCSqGSIb3DQEBBQUAMIGKMQswCQYDVQQGEwJD
          |TjESMBAGA1UECBMJR3Vhbmdkb25nMREwDwYDVQQHEwhTaGVuemhlbjEQMA4GA1UE
          |ChMHVGVuY2VudDEMMAoGA1UECxMDV1hHMRMwEQYDVQQDEwpNbXBheW1jaENBMR8w
          |HQYJKoZIhvcNAQkBFhBtbXBheW1jaEB0ZW5jZW50MB4XDTE1MTAxOTA1MjEwNVoX
          |DTI1MTAxNjA1MjEwNVowgZIxCzAJBgNVBAYTAkNOMRIwEAYDVQQIEwlHdWFuZ2Rv
          |bmcxETAPBgNVBAcTCFNoZW56aGVuMRAwDgYDVQQKEwdUZW5jZW50MQ4wDAYDVQQL
          |EwVNTVBheTEnMCUGA1UEAxQe5YyX5Lqs6Zuq6KeB56eR5oqA5pyJ6ZmQ5YWs5Y+4
          |MREwDwYDVQQEEwgxMDcwNDk2MDCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoC
          |ggEBANii6qYrE8oxeleVO4vJoxP1yq2bS97XuFpTraml3O48306CipNfc0r5DvfR
          |dd+ZAErOSN/YD722mJERUOIeS4LZufkVHOzCpFUAkl8gqJzMuHTx4yuyUK2i2wSc
          |wJRGJL+mcR+9tx+Yas0FYE3iRBAxXPI/wgvyG0py8x1tCGW4kvQYxICtqAvS0Bhk
          |gafLrJw5r3x4XQw7tEdYEHHIDh2hhk8hCfN7/kJRNR1P8H0OVKDLtqE/Yc7Qdy8f
          |UwoDlU7ynyo5+KbH4QpbBRj7wIjalcXywD4MIDqCQl/aEbJLFZz3CwzKQA601qL1
          |qk3iXkXge1M+BZBFmi6mvbZQK58CAwEAAaOCAUYwggFCMAkGA1UdEwQCMAAwLAYJ
          |YIZIAYb4QgENBB8WHSJDRVMtQ0EgR2VuZXJhdGUgQ2VydGlmaWNhdGUiMB0GA1Ud
          |DgQWBBTd5JUSS2u4eFHHNG4lOaGRBO6gYTCBvwYDVR0jBIG3MIG0gBQ+BSb2ImK0
          |FVuIzWR+sNRip+WGdKGBkKSBjTCBijELMAkGA1UEBhMCQ04xEjAQBgNVBAgTCUd1
          |YW5nZG9uZzERMA8GA1UEBxMIU2hlbnpoZW4xEDAOBgNVBAoTB1RlbmNlbnQxDDAK
          |BgNVBAsTA1dYRzETMBEGA1UEAxMKTW1wYXltY2hDQTEfMB0GCSqGSIb3DQEJARYQ
          |bW1wYXltY2hAdGVuY2VudIIJALtUlyu8AOhXMA4GA1UdDwEB/wQEAwIGwDAWBgNV
          |HSUBAf8EDDAKBggrBgEFBQcDAjANBgkqhkiG9w0BAQUFAAOBgQC0iVOh2nV9bo3o
          |lh+noigiCaMXIZmPJe27eG+Bjy074HgaTOVTvWGeSeL1UyR0W2O1oNLJOJGmNyMn
          |trWcGo7Mo78sFXKHZhd73nTAFd0A44FJLvtqSb/ezUdByg/yxTm1J2BIO2j7ZNN/
          |pt89KbZzg9zO4GhFgSUdQtlnBHi4ig==
          |-----END CERTIFICATE-----
        """.stripMargin

      val configString = """
                           |//play.ws.ssl.debug=["certpath", "ssl", "trustmanager"]
                           |play.ws.ssl.protocol="TLSv1"
                           |play.ws.ssl.enabledProtocols=["TLSv1"]
                           |
                           |play.ws.ssl.trustManager = {
                           |  stores = [
                           |    { type: "PEM", data = ${geotrust.pem} }
                           |  ]
                           |}
                         """.stripMargin
      val rawConfig = ConfigFactory.parseString(configString)
      val configWithPem = rawConfig.withValue("geotrust.pem", ConfigValueFactory.fromAnyRef(geoTrustPem))
      val configWithSystemProperties = ConfigFactory.load(configWithPem)
      val playConfiguration = play.api.Configuration(configWithSystemProperties)

      val postBody =
        """<xml>
          |<body>One temp</body>
          |<ip>192.168.0.0</ip>
          |<mch_id>wx86048e56adaf7486</mch_id>
          |<out_trade_no>562db3fc8518a31af06d2518</out_trade_no>
          |<openid>100000</openid><nonce_str>428e62e1a90491092b2b949d84423ca8</nonce_str>
          |<sign>DA0D0785314AF32EDB8793D2A0C1F4AC</sign>
          |<attach>微信支付</attach><appid>wx86048e56adaf7486</appid>
          |<notify_url>http://182.92.168.171:11219/app/payment-webhook/wechat</notify_url>
          |<total_fee>10000</total_fee><trade_type>APP</trade_type></xml>""".stripMargin
      val client = createClient(playConfiguration)
      //      val response = await(client
      //        .url("https://api.mch.weixin.qq.com/pay/unifiedorder").post(postBody))(5.seconds)

      val response = await(WS.url("https://api.mch.weixin.qq.com/pay/unifiedorder")
        .withHeaders("Content-Type" -> "text/xml; charset=utf-8")
        .withRequestTimeout(30000)
        .post(postBody.toString))

      response.status must be_==(200)
      val jsonOutput = response.json
      val result = (jsonOutput \ "tls_version").validate[String]
      result must beLike {
        case JsSuccess(value, path) =>
          value must_== "TLS 1.0"
      }
    }
  }
}
