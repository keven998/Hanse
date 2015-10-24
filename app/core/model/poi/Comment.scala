package core.model.poi

import javax.validation.constraints.{ Min, NotNull }

import core.model.BasicEntity
import core.model.mixin.RatingEnabled
import org.hibernate.validator.constraints.NotBlank

import scala.beans.BeanProperty

/**
 * Created by pengyt on 2015/10/19.
 */
class Comment extends BasicEntity with RatingEnabled {

  /**
   * 用户ID
   */
  @NotNull
  @Min(value = 1)
  @BeanProperty
  var userId: Long = 0

  /**
   * 用户头像
   */
  @BeanProperty
  var authorAvatar: String = null

  /**
   * 用户昵称
   */
  @BeanProperty
  var authorName: String = null

  /**
   * 评价的详情
   */
  @NotBlank
  @BeanProperty
  var contents: String = null

  /**
   * 评论发表时间
   */
  @NotNull
  @BeanProperty
  var publishTime: Long = 0

  /**
   * 评论修改时间
   */
  @NotNull
  @BeanProperty
  var mTime: Long = 0

}