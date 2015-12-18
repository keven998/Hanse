package core.exception

/**
 * 通用的支付异常
 * Created by zephyre on 12/18/15.
 */
class GeneralPaymentException(message: String, cause: Throwable) extends HanseBaseException(message, cause)

object GeneralPaymentException {
  def apply(message: String, cause: Throwable) = new GeneralPaymentException(message, cause)

  def apply(message: String) = new GeneralPaymentException(message, null)

  def apply(cause: Throwable) = new GeneralPaymentException(null, cause)
}
