package com.lvxingpai.model.misc

import javax.validation.constraints.{Min, NotNull}

import com.lvxingpai.model.BasicEntity
import com.lvxingpai.model.mixin.{HotnessEnabled, ImagesEnabled, RatingEnabled}
import org.hibernate.validator.constraints.NotBlank
import org.joda.time.Instant

import scala.beans.{BeanProperty, BooleanBeanProperty}

/**
 * Created by pengyt on 2015/10/19.
 */
class TravelNote extends BasicEntity with ImagesEnabled with RatingEnabled with HotnessEnabled {

  /**
   * 游记标题
   */
  @NotBlank
  @BeanProperty
  var title: String = null

  /**
   * 发表时间
   */
  @NotNull
  @BeanProperty
  var publishTime: Instant = null

  /**
   * 收藏次数
   */
  @Min(value = 0)
  @BeanProperty
  var favorCnt: Int = 0

  /**
   * 评论次数
   */
  @Min(value = 0)
  @BeanProperty
  var commentCnt: Int = 0

  /**
   * 浏览次数
   */
  @Min(value = 0)
  @BeanProperty
  var viewCnt: Int = 0

  /**
   * 分享次数
   */
  @Min(value = 0)
  @BeanProperty
  var shareCnt: Int = 0

  /**
   * 出游的时间
   */
  @NotNull
  @BeanProperty
  var travelTime: Instant = null

  /**
   * 游记摘要
   */
  @NotBlank
  @BeanProperty
  var summary: String = null

  /**
   * 游记正文
   */
  @BeanProperty
  var contents: Seq[(String, String)] = null

  /**
   * 游记来源
   */
  @BeanProperty
  var source: String = null

  /**
   * 是否为精华游记
   */
  @BooleanBeanProperty
  var essence: Boolean = false
}
