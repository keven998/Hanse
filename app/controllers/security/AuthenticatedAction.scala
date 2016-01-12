package controllers.security

import com.lvxingpai.yunkai.UserInfo
import controllers.security.Security.{ AuthenticatedBuilder, AuthenticatedRequest }
import core.utils.HanseResults
import libs.RequestProcessingExtended
import libs.RequestProcessingExtended.WrappedPayload
import play.api.mvc.{ AnyContent, BodyParser, BodyParsers, Result }

import scala.concurrent.{ ExecutionContext, Future }

/**
 * Created by zephyre on 1/12/16.
 */
object AuthenticatedAction extends AuthenticatedBuilder[UserInfo](AuthenticatedBuilder.auth, _ => HanseResults.unauthorized()) {
  def async2(block: AuthenticatedRequest[WrappedPayload[AnyContent], UserInfo] => Future[Result])(implicit ctx: ExecutionContext) = {

    val bodyParser: BodyParser[WrappedPayload[AnyContent]] =
      RequestProcessingExtended.wrappedBodyParser(BodyParsers.parse.anyContent)

    AuthenticatedAction.async(bodyParser)(block)
  }
}

