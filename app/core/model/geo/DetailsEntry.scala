package core.model.geo

import core.model.mixin.ImagesEnabled
import org.hibernate.validator.constraints.NotBlank

import scala.beans.BeanProperty

/**
 * 详情介绍，包含标题、描述和图集
 *
 * Created by pengyt on 2015/10/19.
 */
class DetailsEntry extends ImagesEnabled {

  /**
   * 标题
   */
  @NotBlank
  @BeanProperty
  var title: String = null

  /**
   * 描述
   */
  @NotBlank
  @BeanProperty
  var desc: String = null
}
