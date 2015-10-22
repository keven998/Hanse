package com.lvxingpai.model.trade.order

import javax.validation.constraints.{ Min, NotNull }

import com.lvxingpai.model.BasicEntity
import com.lvxingpai.model.trade.product.Commodity
import org.joda.time.Instant

import scala.beans.BeanProperty
import scala.collection.mutable.{ Map => MutableMap }

/**
 * 订单
 * Created by zephyre on 10/20/15.
 */
class P_Order extends BasicEntity {

  /**
   * 对应的商品
   */
  @NotNull
  @BeanProperty
  var commodity: Commodity = null

  /**
   * 订单总价
   */
  @Min(value = 0)
  @BeanProperty
  var totalPrice: Float = 0

  /**
   * 折扣
   */
  @Min(value = 0)
  @BeanProperty
  var discount: Float = 0

  /**
   * 商品数量
   */
  @Min(value = 1)
  @BeanProperty
  var quantity: Int = 0

  /**
   * 支付信息
   */
  @NotNull
  @BeanProperty
  var payments: MutableMap[PaymentVendor.Value, Prepay] = MutableMap()

  /**
   * 订单状态
   */
  @NotNull
  @BeanProperty
  var status: OrderStatus.Value = null

  /**
   * 下单时间
   */
  @NotNull
  @BeanProperty
  var orderTime: Instant = null

  /**
   * 订单信息更新时间
   */
  @NotNull
  @BeanProperty
  var updateTime: Instant = null
}
