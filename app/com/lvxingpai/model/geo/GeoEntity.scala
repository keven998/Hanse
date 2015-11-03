package com.lvxingpai.model.geo

import org.bson.types.ObjectId
import org.hibernate.validator.constraints.NotBlank
import org.mongodb.morphia.annotations.{ Entity, Id }

import scala.beans.BeanProperty

/**
 * Created by zephyre on 10/27/15.
 */
@Entity
abstract class GeoEntity {
  @Id
  var id: ObjectId = null

  /**
   * 中文名称
   */
  @NotBlank
  @BeanProperty
  var zhName: String = null

  /**
   * 英文名称
   */
  @NotBlank
  @BeanProperty
  var enName: String = null
}
