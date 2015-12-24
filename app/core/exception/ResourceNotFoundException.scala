package core.exception

/**
 * Created by zephyre on 12/17/15.
 */
class ResourceNotFoundException(message: String, cause: Throwable) extends RuntimeException(message, cause) {
  def this() = this(null, null)

  def this(message: String) = this(message, null)

  def this(cause: Throwable) = this(null, cause)
}

object ResourceNotFoundException {
  def apply(message: String, cause: Throwable) = new ResourceNotFoundException(message, cause)

  def apply(message: String) = new ResourceNotFoundException(message)

  def apply(cause: Throwable) = new ResourceNotFoundException(cause)

  def apply() = new ResourceNotFoundException()
}
