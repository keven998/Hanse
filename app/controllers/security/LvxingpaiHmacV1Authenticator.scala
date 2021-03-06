package controllers.security

import java.net.URLEncoder
import java.util.Locale
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

import com.lvxingpai.yunkai.{ UserInfoProp, NotFoundException, UserInfo }
import com.twitter.util
import com.twitter.util.Base64StringEncoder
import Security.AuthInfo
import core.security.UserRole
import libs.RequestProcessingExtended.WrappedPayload
import org.joda.time.format.DateTimeFormat
import play.api.Play
import play.api.mvc.Request

import scala.concurrent.Future

/**
 * Created by zephyre on 1/12/16.
 */
class LvxingpaiHmacV1Authenticator extends Authenticator {
  /**
   * 查找用户的secret key
   * @param userId
   * @return
   */
  protected def getSecretInfo(userId: Long): Future[Option[(UserInfo, String)]] = {
    import com.lvxingpai.yunkai.Userservice.{ FinagledClient => YunkaiClient }
    import core.misc.Implicits.TwitterConverter._
    import play.api.Play.current

    val yunkai = Play.application.injector instanceOf classOf[YunkaiClient]

    val future = (for {
      userInfo <- yunkai.getUserById(userId, Some(Seq(UserInfoProp.UserId, UserInfoProp.NickName, UserInfoProp.Avatar)))
      secretKey <- yunkai.getUserSecretKey(userId)
    } yield {
      Some((userInfo, secretKey))
    }) rescue {
      case _: NotFoundException => util.Future[Option[(UserInfo, String)]](None)
    }

    twitterToScalaFuture(future)
  }

  /**
   * 将字典转换成url-encoded格式
   * @param toLowerCase 是否转换为小写来处理
   * @return
   */
  private def map2Message(input: Map[String, Seq[String]], toLowerCase: Boolean): String = {
    // key转换为小写
    val seq = if (toLowerCase) {
      input.toSeq map {
        case (t1, t2) => t1.toLowerCase() -> t2
      }
    } else {
      input.toSeq
    }
    seq.sortBy(_._1) map {
      case (key, valueSeq) =>
        val value = valueSeq mkString ""
        s"$key=${URLEncoder.encode(value, "utf-8")}"
    } mkString "&"
  }

  /**
   * 对签名进行验证
   * @return
   */
  private def verifySignature[A](request: Request[A], secretKey: String, signature: String): Boolean = {
    val headerKeys = Seq("Date", "X-Lvxingpai-Id") map (_.toLowerCase()) // ++ (request.headers get "SignatureHeaders"  map (_.split(",") map (_.trim)))
    val requestHeaderKeys = request.headers.keys map (_.toLowerCase())
    if (!headerKeys.forall(header => requestHeaderKeys contains header)) {
      // 有些字段不存在
      return false
    }

    // 检查日期
    val formatter = DateTimeFormat.forPattern("EEE, dd MMM yyyy HH:mm:ss 'GMT'").withZoneUTC().withLocale(Locale.US)
    val requestTime = formatter.parseMillis(request.headers.get("Date").get)
    // 最多相差五分钟
    val maxTimeDiff = 5 * 60 * 1000L
    if (Math.abs(requestTime - System.currentTimeMillis()) > maxTimeDiff) {
      return false
    }

    val queryStringMessage = map2Message(request.queryString, toLowerCase = false)
    // 将request.headers中, 符合headerKeys的筛选出来
    val headerSeq = request.headers.toMap.toSeq map {
      case (t1, t2) => t1.toLowerCase() -> t2
    } filter (headerKeys contains _._1)
    val headerMessage = map2Message(Map(headerSeq: _*), toLowerCase = true)

    val bodyMessage = request.body match {
      case payload: WrappedPayload[_] =>
        Base64StringEncoder.encode(payload.raw)
      case _ =>
        ""
    }

    val sb = new StringBuilder(s"URI=/app${request.path},Headers=$headerMessage")
    if (queryStringMessage.nonEmpty) {
      sb.append(s",QueryString=$queryStringMessage")
    }
    if (bodyMessage.nonEmpty) {
      sb.append(s",Body=$bodyMessage")
    }
    val signatureMessage = sb.toString()

    // 开始HMAC
    val keySpec = new SecretKeySpec(secretKey.getBytes(), "HmacSHA256")
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(keySpec)
    val result = mac.doFinal(signatureMessage.getBytes())

    Base64StringEncoder.encode(result) == signature
  }

  override def authenticate[A](request: Request[A], authMessage: String): Future[AuthInfo[UserInfo]] = {
    import scala.concurrent.ExecutionContext.Implicits.global

    (for {
      userId <- request.headers get "X-Lvxingpai-Id" map (_.toLong)
    } yield {
      getSecretInfo(userId) map (result => {
        result map (entry => {
          val user = entry._1
          val secretKey = entry._2

          // 验证签名
          val pattern = """Signature=([^,]+)""".r
          val signature = pattern.findFirstMatchIn(authMessage) map (_.subgroups.head) getOrElse ""
          // 签名是否验证通过
          val verified = verifySignature(request, secretKey, signature)

          AuthInfo[UserInfo](authProvided = true, if (verified) Set(UserRole.User) else Set(),
            if (verified) Some(user) else None)
        }) getOrElse AuthInfo[UserInfo](authProvided = true, Set(), None) // 没有找到相应的secret key
      })
    }) getOrElse {
      // 没有找到UserId
      Future(AuthInfo[UserInfo](authProvided = true, Set(), None))
    }
  }

}
