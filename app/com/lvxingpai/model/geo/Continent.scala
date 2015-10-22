package com.lvxingpai.model.geo

import javax.validation.constraints.Pattern

import com.lvxingpai.model.BasicEntity
import org.hibernate.validator.constraints.NotBlank

import scala.beans.BeanProperty

/**
 * 洲
 * Created by pengyt on 2015/10/20.
 */
class Continent extends BasicEntity {

  /**
   * 洲中文名
   */
  @NotBlank
  @BeanProperty
  var zhName: String = null

  /**
   * 洲英文名
   */
  @NotBlank
  @BeanProperty
  var enName: String = null

  /**
   * 洲代码
   */
  @NotBlank
  @Pattern(regexp = "[A-Z]{2}")
  @BeanProperty
  var code: String = null
}
