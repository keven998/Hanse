package controllers.security

import com.lvxingpai.yunkai.UserInfo
import controllers.security.Security.AuthInfo
import play.api.mvc.Request

import scala.concurrent.Future

/**
 * Created by zephyre on 1/12/16.
 */
trait Authenticator {
  /**
   * 签名验证
   * @return
   */
  def authenticate[A](request: Request[A], authMessage: String): Future[AuthInfo[UserInfo]]
}
