package com.lvxingpai.model.poi

import scala.beans.BeanProperty

/**
 * Created by pengyt on 2015/10/19.
 */
class ViewSpot extends AbstractPOI {

  /**
   * 景点的类型，比如：历史文化
   */
  var viewSpotStyle: Set[String] = Set()

  /**
   * AAA景区：3
   * AAAA景区：4
   * 等级
   */
  @BeanProperty
  var level: Int = 0

  /**
   * 建议旅游季节
   */
  @BeanProperty
  var travelMonth: String = null

  /**
   * 建议游玩时间
   */
  @BeanProperty
  var timeCostDesc: String = null
}