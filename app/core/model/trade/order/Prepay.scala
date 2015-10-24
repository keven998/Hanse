package core.model.trade.order

import javax.validation.constraints.{ Min, NotNull }

import core.model.BasicEntity

import scala.beans.BeanProperty

/**
 * 在第三方支付系统中，和Order对应的prepay对象
 *
 * Created by zephyre on 10/20/15.
 */
class Prepay extends BasicEntity {

  /**
   * 对应于哪个支付平台
   */
  @NotNull
  @BeanProperty
  var vendor: String = null

  /**
   * 支付金额
   */
  @Min(value = 0)
  @BeanProperty
  var amount: Float = 0

  /**
   * 生成Prepay的时间
   */
  @NotNull
  @BeanProperty
  var timestamp: Long = 0
}
