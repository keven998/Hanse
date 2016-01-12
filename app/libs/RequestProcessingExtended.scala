package libs

import play.api.libs.iteratee.Enumerator
import play.api.mvc._

import scala.concurrent.{ ExecutionContext, Future }

/**
 * 扩展了处理请求的机制. 可以返回请求的raw byte
 *
 * Created by zephyre on 1/12/16.
 */

object RequestProcessingExtended {

  case class RawRequest[A](request: Request[A], raw: Array[Byte]) extends WrappedRequest[A](request)

  case class WrappedPayload[A](wrapped: A, raw: Array[Byte])

  def wrappedBodyParser[A](wrapped: BodyParser[A])(implicit exeCtx: ExecutionContext): BodyParser[WrappedPayload[A]] = BodyParser("raw-memo") { request =>
    BodyParsers.parse.raw(request).mapM {
      case Left(result) =>
        Future.successful(Left(result))

      case Right(buffer) =>
        val bytes = buffer.asBytes().getOrElse(Array.empty[Byte])

        Enumerator(bytes)(wrapped(request)) flatMap { i =>
          i.run
        } map {
          case Left(result) =>
            Left(result)

          case Right(a) =>
            val rawBody = bytes
            Right(WrappedPayload(a, rawBody))
        }
    }
  }
}

