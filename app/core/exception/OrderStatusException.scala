package core.exception

/**
 * 订单状态异常. 比如, 对一个已经过期的账单进行支付等操作
 *
 * Created by zephyre on 12/17/15.
 */
class OrderStatusException(message: String, cause: Throwable) extends RuntimeException(message, cause) {
  def this() = this(null, null)

  def this(message: String) = this(message, null)

  def this(cause: Throwable) = this(null, cause)
}

object OrderStatusException {
  def apply(message: String, cause: Throwable) = new OrderStatusException(message, cause)

  def apply(message: String) = new OrderStatusException(message)

  def apply(cause: Throwable) = new OrderStatusException(cause)

  def apply() = new OrderStatusException()
}
