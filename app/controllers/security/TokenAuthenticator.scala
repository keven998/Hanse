package controllers.security

import com.lvxingpai.yunkai.{ UserInfoProp, NotFoundException, UserInfo }
import Security.AuthInfo
import core.security.UserRole
import play.api.inject.BindingKey
import play.api.mvc.Request
import play.api.{ Configuration, Play }

import scala.collection.JavaConversions._
import scala.concurrent.Future
import scala.util.{ Failure, Success, Try }

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
      tokens <- {
        // 获得token列表
        val confKey = BindingKey(classOf[Configuration]) qualifiedWith "default"
        val conf = Play.application.injector instanceOf confKey
        conf getConfig "security.auth.tokens" orElse Some(Configuration.empty)
      }
    } yield {
      val roles = scala.collection.mutable.Set[UserRole.Value]()
      // security.auth.tokens下面的每一个subkey, 都定义了一个role. 如果用户提供了相应的token, 表示获得这个role的权限
      tokens.subKeys foreach (subKey => {
        val tokenEntries = tokens getStringList subKey map (_.toSeq) getOrElse Seq()
        if (tokenEntries contains authMessage) {
          // 尝试赋予相应的role
          Try(UserRole withName subKey) match {
            case Success(v) => roles += v
            case Failure(_) =>
          }
        }
      })

      // 在使用Token的情况下, 必须有roles
      if (roles.isEmpty) {
        Future.successful(unauth)
      } else {
        for {
          userInfo <- {
            request.headers get "X-Lvxingpai-Id" map (v => {
              yunkai.getUserById(v.toLong, Some(Seq(UserInfoProp.UserId, UserInfoProp.NickName, UserInfoProp.Avatar))) map Option.apply recover {
                case _: NotFoundException => None
              }
            }) getOrElse Future.successful(None)
          }
        } yield {
          // 如果查到了user信息, 需要获得user权限
          if (userInfo.nonEmpty)
            roles += UserRole.User

          AuthInfo(authProvided = true, roles.toSet, userInfo)
        }
      }
    }) getOrElse Future.successful(unauth)
  }
}
