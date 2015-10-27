package com.lvxingpai.model.misc

import javax.validation.constraints.Min

import org.hibernate.validator.constraints.NotBlank
import org.mongodb.morphia.annotations.Embedded

import scala.beans.BeanProperty

/**
 * 图像数据
 *
 * Created by zephyre on 10/20/15.
 */
@Embedded
class ImageItem {

  /**
   * 标题
   */
  @BeanProperty
  var caption: String = null

  /**
   * 图像的主键
   */
  @NotBlank
  @BeanProperty
  var key: String = null

  /**
   * 图像放在哪个bucket之中
   */
  @BeanProperty
  var bucket: String = null

  /**
   * 图像宽度
   */
  @Min(value = 0)
  @BeanProperty
  var width: Int = 0

  /**
   * 图像高度
   */
  @Min(value = 0)
  @BeanProperty
  var height: Int = 0
}

