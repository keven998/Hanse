package controllers.security

import com.lvxingpai.yunkai.{ NotFoundException, UserInfo }
import controllers.security.Security.AuthInfo
import play.api.inject.BindingKey
import play.api.{ Configuration, Play }
import play.api.mvc.Request
import scala.collection.JavaConversions._

import scala.concurrent.Future

/**
 * Created by zephyre on 1/12/16.
 */
class TokenAuthenticator extends Authenticator {
  /**
   * 签名验证
   * @return
   */
  override def authenticate[A](request: Request[A], authMessage: String): Future[AuthInfo[UserInfo]] = {
    import com.lvxingpai.yunkai.Userservice.{ FinagledClient => YunkaiClient }
    import core.misc.Implicits.TwitterConverter._
    import play.api.Play.current
    import com.twitter.util

    val yunkai = Play.application.injector instanceOf classOf[YunkaiClient]

    // 登录失败时, 返回的AuthInfo
    val unauth = AuthInfo[UserInfo](authProvided = true, None)

    (for {
      userId <- request.headers get "X-Lvxingpai-Id" map (_.toLong)
      verified <- {
        // 获得token列表
        val confKey = BindingKey(classOf[Configuration]) qualifiedWith "default"
        val conf = Play.application.injector instanceOf confKey
        val tokens = conf getStringList "security.auth.tokens" map (_.toSeq) getOrElse Seq()
        Some(tokens contains authMessage)
      }
    } yield {
      if (verified) {
        val future = yunkai.getUserById(userId) map (user => {
          AuthInfo(authProvided = true, Some(user))
        }) rescue {
          case _: NotFoundException => util.Future(unauth)
        }
        twitterToScalaFuture(future)
      } else {
        Future.successful(unauth)
      }
    }) getOrElse Future.successful(unauth)
  }
}
