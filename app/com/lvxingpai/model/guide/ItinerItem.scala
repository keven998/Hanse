package com.lvxingpai.model.guide

import javax.validation.constraints.{NotNull, Min}

import com.lvxingpai.model.poi.AbstractPOI
import org.hibernate.validator.constraints.NotBlank

import scala.beans.BeanProperty

/**
 * Created by pengyt on 2015/10/21.
 */
class ItinerItem {

  /**
   * 行程第几天
   */
  @Min(value = 0)
  @BeanProperty
  var dayIndex: Int = 0

  /**
   * poi类型
   */
  @NotBlank
  @BeanProperty
  var poiType: String = null

  /**
   * 去哪个poi
   */
  @NotNull
  @BeanProperty
  var poi: AbstractPOI = null
}
