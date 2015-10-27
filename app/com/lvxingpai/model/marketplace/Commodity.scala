package com.lvxingpai.model.marketplace

import javax.validation.constraints.{Max, Min, NotNull}

import org.hibernate.validator.constraints.NotBlank
import org.mongodb.morphia.annotations.Id

import scala.beans.BeanProperty

/**
 * 商品
 *
 * Created by zephyre on 10/20/15.
 */
class Commodity {
  @Id
  var id = null

  /**
   * 商家
   */
  @NotNull
  @BeanProperty
  var seller: Seller = null

  /**
   * 商品描述
   */
  @NotBlank
  @Max(1024)
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
