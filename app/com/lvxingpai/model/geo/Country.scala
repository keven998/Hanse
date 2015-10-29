package com.lvxingpai.model.geo

import javax.validation.constraints.Pattern

import org.hibernate.validator.constraints.NotBlank
import org.mongodb.morphia.annotations.Entity

import scala.beans.BeanProperty

/**
 * Created by zephyre on 10/27/15.
 */
@Entity
class Country extends GeoEntity {
  /**
   * ISO 3166-2标准的国家代码
   */
  @NotBlank
  @Pattern(regexp = "[A-Z]{2}")
  @BeanProperty
  var code: String = null

  /**
   * ISO 3166-3标准的国家代码
   */
  @NotBlank
  @Pattern(regexp = "[A-Z]{3}")
  @BeanProperty
  var code3: String = null
}
