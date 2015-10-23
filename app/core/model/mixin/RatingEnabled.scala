package core.model.mixin

import javax.validation.constraints.{ Min, Max }

import scala.beans.BeanProperty

/**
 * 评分, 值在0到1之间
 * Created by pengyt on 2015/10/21.
 */
trait RatingEnabled {

  @Max(value = 1)
  @Min(value = 0)
  @BeanProperty
  var rating: Double = 0.0
}
