package controllers.security

import com.lvxingpai.yunkai.{ NotFoundException, UserInfo }
import controllers.security.Security.AuthInfo
import play.api.inject.BindingKey
import play.api.mvc.Request
import play.api.{ Configuration, Play }

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
    import scala.concurrent.ExecutionContext.Implicits.global

    val yunkai = Play.application.injector instanceOf classOf[YunkaiClient]

    // 登录失败时, 返回的AuthInfo
    val unauth = AuthInfo[UserInfo](authProvided = true, Set(), None)

    (for {
      userId <- request.headers get "X-Lvxingpai-Id" map (_.toLong)
      tokens <- {
        // 获得token列表
        val confKey = BindingKey(classOf[Configuration]) qualifiedWith "default"
        val conf = Play.application.injector instanceOf confKey
        conf getConfig "security.auth.tokens" orElse Some(Configuration.empty)
      }
      adminTokens <- tokens getStringList "admin" map (_.toSeq) orElse Some(Seq())
      userTokens <- tokens getStringList "user" map (_.toSeq) orElse Some(Seq())
    } yield {
      val isAdmin = tokens getStringList "admin" map (_.toSeq) getOrElse Seq() contains authMessage
      val isUser = tokens getStringList "user" map (_.toSeq) getOrElse Seq() contains authMessage

      val roles = scala.collection.mutable.Set[Security.UserRole.Value]()
      if (isAdmin)
        roles += Security.UserRole.Admin
      if (isUser)
        roles += Security.UserRole.User

      if (roles.isEmpty) {
        Future.successful(unauth)
      } else {
        for {
          userInfo <- {
            request.headers get "X-Lvxingpai-Id" map (v => {
              yunkai.getUserById(v.toLong) map Option.apply recover {
                case _: NotFoundException => None
              }
            }) getOrElse Future.successful(None)
          }
        } yield {
          AuthInfo(authProvided = true, roles.toSet, userInfo)
        }
      }
    }) getOrElse Future.successful(unauth)
  }
}
