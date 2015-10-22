package com.lvxingpai.model.misc

import javax.validation.constraints.Min

import org.hibernate.validator.constraints.NotBlank

import scala.beans.BeanProperty

/**
 * 图像数据
 *
 * Created by zephyre on 10/20/15.
 */
class ImageItem {

  /**
   * 图像的裁剪信息
   *
   * @param left
   * @param top
   * @param right
   * @param bottom
   */
  case class CropHint(left: Int, top: Int, right: Int, bottom: Int)

  /**
   * 标题
   */
  @BeanProperty
  var caption: String = null

  /**
   * 裁剪信息
   */
  @BeanProperty
  var cropHint: CropHint = null

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
