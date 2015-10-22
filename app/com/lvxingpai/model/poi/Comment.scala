package com.lvxingpai.model.poi

import javax.validation.constraints.{Min, NotNull}

import com.lvxingpai.model.BasicEntity
import com.lvxingpai.model.mixin.RatingEnabled
import org.hibernate.validator.constraints.NotBlank
import org.joda.time.Instant

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
  var authorAvatar: Option[String] = None

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
  var publishTime: Instant = null

  /**
   * 评论修改时间
   */
  @NotNull
  @BeanProperty
  var mTime: Instant = null

}