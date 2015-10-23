package com.lvxingpai.model.trade.product

import com.lvxingpai.model.trade.product.{ P_Commodity => BasicCommodity }

import scala.beans.BeanProperty

/**
 * 商品
 *
 * Created by topy on 10/22/15.
 */
class Commodity extends BasicCommodity {

  /**
   * 商品价格
   */
  @BeanProperty
  var price: Int = 0
}
