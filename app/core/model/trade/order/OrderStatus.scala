package core.model.trade.order

/**
 * 订单状态
 * Created by zephyre on 10/20/15.
 */
object OrderStatus {
  /**
   * 已下单但未支付
   */
  val Pending = "pending"

  /**
   * 已支付
   */
  val Paid = "paid"

  /**
   * 已申请退款
   */
  val RefundApplied = "refundApplied"

  /**
   * 正在审核退款申请
   */
  val RefundReviewing = "refundReviewing"

  /**
   * 退款成功
   */
  val Refunded = "refunded"

  /**
   * 订单已取消
   */
  val Canceled = "canceled"

  /**
   * 订单已消费，等待用户确认
   */
  val Committed = "committed"

  /**
   * 订单已完成
   */
  val Finished = "finished"
}
