package com.lvxingpai.model.misc

import javax.validation.constraints.Min

import com.lvxingpai.model.BasicEntity
import com.lvxingpai.model.mixin.ImagesEnabled
import com.lvxingpai.model.poi.Description
import org.joda.time.Instant

import scala.beans.BeanProperty

/**
 * web用的推荐内容
 * Created by pengyt on 2015/10/21.
 */
class Recommendation extends BasicEntity with ImagesEnabled {

  /**
   * 名称
   */
  @BeanProperty
  var name: String = null

  /**
   * 图片
   */
  @BeanProperty
  var imageList: Seq[String] = Seq()

  /**
   * 热门景点
   */
  @BeanProperty
  var hotVs: Int = 0

  /**
   * 热门城市
   */
  @BeanProperty
  var hotCity: Int = 0

  /**
   * 新鲜出炉
   */
  @BeanProperty
  var newItemWeight: Int = 0

  /**
   * 不可不去
   */
  @BeanProperty
  var mustGoWeight: Int = 0

  /**
   * 小编推荐
   */
  @BeanProperty
  var editorWeight: Int = 0

  /**
   * 人气之旅
   */
  @BeanProperty
  var popularityWeight: Int = 0

  /**
   * 路线编辑的昵称
   */
  @BeanProperty
  var editorNickName: String = null

  /**
   * 路线编辑的头像
   */
  @BeanProperty
  var editorAvatar: String = null

  /**
   * 路线编辑的头像
   */
  @BeanProperty
  var editorDate: Instant = null

  /**
   * 浏览量
   */
  @Min(value = 0)
  @BeanProperty
  var planViews: Int = 0

  /**
   * 介绍
   */
  @BeanProperty
  var description: Description = null

  /**
   * 理由
   */
  @BeanProperty
  var reason: String = null
}
