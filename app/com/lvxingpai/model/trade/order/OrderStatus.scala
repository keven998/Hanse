package com.lvxingpai.model.trade.order

/**
 * 订单状态
 * Created by zephyre on 10/20/15.
 */
object OrderStatus extends Enumeration {
  /**
   * 已下单但未支付
   */
  val Pending = Value("pending")

  /**
   * 已支付
   */
  val Paid = Value("paid")

  /**
   * 已申请退款
   */
  val RefundApplied = Value("refundApplied")

  /**
   * 正在审核退款申请
   */
  val RefundReviewing = Value("refundReviewing")

  /**
   * 退款成功
   */
  val Refunded = Value("refunded")

  /**
   * 订单已取消
   */
  val Canceled = Value("canceled")

  /**
   * 订单已消费，等待用户确认
   */
  val Committed = Value("committed")

  /**
   * 订单已完成
   */
  val Finished = Value("finished")
}
