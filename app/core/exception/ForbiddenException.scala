package core.exception

/**
 * 权限错误
 *
 * Created by zephyre on 1/22/16.
 */
class ForbiddenException(message: String, cause: Throwable) extends RuntimeException(message, cause) {
  def this() = this(null, null)

  def this(message: String) = this(message, null)

  def this(cause: Throwable) = this(null, cause)
}

object ForbiddenException {
  def apply(message: String, cause: Throwable) = new ForbiddenException(message, cause)

  def apply(message: String) = new ForbiddenException(message)

  def apply(cause: Throwable) = new ForbiddenException(cause)

  def apply() = new ForbiddenException()
}

