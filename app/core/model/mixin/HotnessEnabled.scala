package core.model.mixin

import javax.validation.constraints.Min

import scala.beans.BeanProperty

/**
 * 热门程度
 * Created by pengyt on 2015/10/21.
 */
trait HotnessEnabled {

  @Min(value = 0)
  @BeanProperty
  var hotness: Double = 0.0
}
