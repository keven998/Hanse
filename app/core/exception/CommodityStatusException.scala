package core.exception

/**
 * 修改商品状态时，传入了一个非法值
 *
 * Created by topy on 3/9/16.
 */
class CommodityStatusException(message: String, cause: Throwable) extends RuntimeException(message, cause) {
  def this() = this(null, null)

  def this(message: String) = this(message, null)

  def this(cause: Throwable) = this(null, cause)
}

object CommodityStatusException {
  def apply(message: String, cause: Throwable) = new CommodityStatusException(message, cause)

  def apply(message: String) = new CommodityStatusException(message)

  def apply(cause: Throwable) = new CommodityStatusException(cause)

  def apply() = new CommodityStatusException()
}
