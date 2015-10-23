package core.model.trade.order

import java.util
import javax.validation.constraints.{ Min, NotNull }

import core.model.BasicEntity
import core.model.trade.product.Commodity

import scala.beans.BeanProperty

/**
 * 订单
 * Created by zephyre on 10/20/15.
 */
class Order extends BasicEntity {

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
  var payments: util.HashMap[String, Prepay] = null

  /**
   * 订单状态
   */
  @NotNull
  @BeanProperty
  var status: String = null

  /**
   * 下单时间
   */
  @NotNull
  @BeanProperty
  var orderTime: Long = 0

  /**
   * 订单信息更新时间
   */
  @NotNull
  @BeanProperty
  var updateTime: Long = 0
}
object Order {

  val FD_ID = "id"
  val FD_COMMODITY = "commodity"
}