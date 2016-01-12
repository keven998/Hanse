package core.exception

/**
 * Created by topy on 2016/1/11.
 */
class AlipayRefundException(message: String, cause: Throwable) extends HanseBaseException(message, cause)

object AlipayRefundException {
  def apply(message: String, cause: Throwable) = new AlipayRefundException(message, cause)

  def apply(message: String) = new GeneralPaymentException(message, null)

  def apply(cause: Throwable) = new GeneralPaymentException(null, cause)
}
