package com.lvxingpai.model.trade.product

import javax.validation.constraints.NotNull

import com.lvxingpai.model.BasicEntity
import com.lvxingpai.model.trade.saler.Saler
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
}
