package com.lvxingpai.model.mixin

import javax.validation.constraints.NotNull

import com.lvxingpai.model.misc.ImageItem

import scala.beans.BeanProperty

/**
 * 图像列表
 * Created by zephyre on 10/20/15.
 */
trait ImagesEnabled {
  @NotNull
  @BeanProperty
  var images: Seq[ImageItem] = Seq()

}
