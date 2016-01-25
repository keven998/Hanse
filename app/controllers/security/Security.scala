package controllers.security

import com.lvxingpai.yunkai.UserInfo
import libs.RequestProcessingExtended.RawRequest
import play.api.Play
import play.api.mvc.Results._
import play.api.mvc._

import scala.concurrent.Future

/**
 * Helpers to create secure actions.
 */
object Security {

  object UserRole extends Enumeration {
    val Admin = Value("admin")
    val User = Value("user")
  }

  case class AuthInfo[U](authProvided: Boolean, roles: Set[UserRole.Value], user: Option[U])

  /**
   * An authenticated request
   *
   * @param auth The authentication info
   */
  class AuthenticatedRequest[A, U](val auth: AuthInfo[U], request: Request[A], raw: Array[Byte])
    extends RawRequest[A](request, raw)

  /**
   * An authenticated action builder.
   *
   * This can be used to create an action builder, like so:
   *
   * {{{
   * // in a Security trait
   * object Authenticated extends AuthenticatedBuilder(req => getUserFromRequest(req))
   *
   * // then in a controller
   * def index = Authenticated { implicit request =>
   *   Ok("Hello " + request.user)
   * }
   * }}}
   *
   * It can also be used from an action builder, for example:
   *
   * {{{
   * class AuthenticatedDbRequest[A](val user: User,
   *                                 val conn: Connection,
   *                                 request: Request[A]) extends WrappedRequest[A](request)
   *
   * object Authenticated extends ActionBuilder[AuthenticatedDbRequest] {
   *   def invokeBlock[A](request: Request[A], block: (AuthenticatedDbRequest[A]) => Future[Result]) = {
   *     AuthenticatedBuilder(req => getUserFromRequest(req)).authenticate(request, { authRequest: AuthenticatedRequest[A, User] =>
   *       DB.withConnection { conn =>
   *         block(new AuthenticatedDbRequest[A](authRequest.user, conn, request))
   *       }
   *     })
   *   }
   * }
   * }}}
   *
   * @param authinfo The function that looks up the user info.
   * @param onUnauthorized The function to get the result for when no authenticated user can be found.
   */
  class AuthenticatedBuilder[U](
    authinfo: Request[_] => Future[AuthInfo[U]],
    onUnauthorized: RequestHeader => Result = _ => Unauthorized(views.html.defaultpages.unauthorized())
  )
      extends ActionBuilder[({ type R[A] = AuthenticatedRequest[A, U] })#R] {

    override def invokeBlock[A](request: Request[A], block: (AuthenticatedRequest[A, U]) => Future[Result]) =
      authenticate(request, block)

    /**
     * Authenticate the given block.
     */
    def authenticate[A](request: Request[A], block: (AuthenticatedRequest[A, U]) => Future[Result]) = {
      import scala.concurrent.ExecutionContext.Implicits.global

      authinfo(request) flatMap (auth => {
        if (auth.authProvided) {
          if (auth.roles.nonEmpty) {
            block(new AuthenticatedRequest(auth, request, null))
          } else {
            Future.successful(onUnauthorized(request))
          }
        } else {
          // Doesn't provided any authentication
          block(new AuthenticatedRequest(auth, request, null))
        }
      })
    }
  }

  /**
   * An authenticated action builder.
   *
   * This can be used to create an action builder, like so:
   *
   * {{{
   * // in a Security trait
   * object Authenticated extends AuthenticatedBuilder(req => getUserFromRequest(req))
   *
   * // then in a controller
   * def index = Authenticated { implicit request =>
   *   Ok("Hello " + request.user)
   * }
   * }}}
   *
   * It can also be used from an action builder, for example:
   *
   * {{{
   * class AuthenticatedDbRequest[A](val user: User,
   *                                 val conn: Connection,
   *                                 request: Request[A]) extends WrappedRequest[A](request)
   *
   * object Authenticated extends ActionBuilder[AuthenticatedDbRequest] {
   *   def invokeBlock[A](request: Request[A], block: (AuthenticatedDbRequest[A]) => Future[Result]) = {
   *     AuthenticatedBuilder(req => getUserFromRequest(req)).authenticate(request, { authRequest: AuthenticatedRequest[A, User] =>
   *       DB.withConnection { conn =>
   *         block(new AuthenticatedDbRequest[A](authRequest.user, conn, request))
   *       }
   *     })
   *   }
   * }
   * }}}
   */
  object AuthenticatedBuilder {

    /**
     * 身份验证
     * @param request
     * @return
     */
    def auth(request: Request[_]): Future[AuthInfo[UserInfo]] = {
      import com.lvxingpai.yunkai.Userservice.{ FinagledClient => YunkaiClient }
      import play.api.Play.current

      val injector = Play.application.injector
      val yunkaiClient = injector instanceOf classOf[YunkaiClient]

      (for {
        authHeader <- request.headers get "Authorization" map (_.trim)
      } yield {
        val authScheme = authHeader.split("\\s+", 2).head
        val authMessage = authHeader.split("\\s+", 2).last

        val authenticator = authScheme match {
          case "LVXINGPAI-v1-HMAC-SHA256" => new LvxingpaiHmacV1Authenticator()
          case "Token" => new TokenAuthenticator()
          case _ => new Authenticator {
            // 未知的auth scheme, 返回401
            override def authenticate[A](request: Request[A], authMessage: String) =
              Future.successful(AuthInfo[UserInfo](authProvided = true, Set(), None))
          }
        }
        authenticator.authenticate(request, authMessage)
      }) getOrElse Future.successful(AuthInfo[UserInfo](authProvided = false, Set(), None)) // 没有提供auth信息, 视为未登录
    }
  }
}

