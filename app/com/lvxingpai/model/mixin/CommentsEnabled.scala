package com.lvxingpai.model.mixin

import javax.validation.constraints.NotNull

import com.lvxingpai.model.poi.Comment

import scala.beans.BeanProperty

/**
 * 评论列表
 * Created by pengyt on 2015/10/21.
 */
trait CommentsEnabled {

  @NotNull
  @BeanProperty
  var comments: Seq[Comment] = Seq()
}
