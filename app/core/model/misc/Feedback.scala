package core.model.misc

import javax.validation.constraints.Min

import core.model.BasicEntity
import org.hibernate.validator.constraints.NotBlank

import scala.beans.BeanProperty

/**
 * Created by pengyt on 2015/10/21.
 */
class Feedback extends BasicEntity {

  /**
   * 用户id
   */
  @NotBlank
  @Min(value = 1)
  @BeanProperty
  var userId: Long = 0

  /**
   * 反馈内容
   */
  @BeanProperty
  var body: String = null

  /**
   * 反馈时间
   */
  @BeanProperty
  var time: Long = 0

  /**
   * 从哪个App反馈过来的, 例如：旅行派
   */
  @BeanProperty
  var origin: String = null
}
