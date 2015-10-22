package com.lvxingpai.model.trade.order

/**
 * 第三方支付平台的枚举类型
 * Created by zephyre on 10/20/15.
 */
object PaymentVendor extends Enumeration {
  val Wechat = Value("wechat")
  val Alipay = Value("alipay")
  val UnionPay = Value("unionpay")
}
