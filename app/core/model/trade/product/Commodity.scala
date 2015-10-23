package core.model.trade.product

import javax.validation.constraints.{ Min, NotNull }

import core.model.BasicEntity
import core.model.trade.saler.Saler
import org.hibernate.validator.constraints.NotBlank

import scala.beans.BeanProperty

/**
 * 商品
 *
 * Created by zephyre on 10/20/15.
 */
class Commodity extends BasicEntity {
  /**
   * 商家
   */
  @NotNull
  @BeanProperty
  var saler: Saler = null

  /**
   * 商品描述
   */
  @NotBlank
  @BeanProperty
  var title: String = null

  /**
   * 商品详情
   */
  @NotBlank
  @BeanProperty
  var detail: String = null

  /**
   * 商品价格
   */
  @Min(value = 0)
  @BeanProperty
  var price: Float = 0.0f
}
object Commodity {

  val FD_ID = "id"

  def apply(saler: Saler, title: String, detail: String, price: Float): Commodity = {
    val commodity = new Commodity()
    commodity.saler = saler
    commodity.title = title
    commodity.detail = detail
    commodity.price = price
    commodity
  }
}