package com.lvxingpai.model.guide

import javax.validation.constraints.Min

import org.joda.time.Instant

import scala.beans.BeanProperty

/**
 * 攻略
 * Created by pengyt on 2015/10/21.
 */
class Guide extends AbstractGuide {

  /**
   * 用户id
   */
  @Min(value = 1)
  @BeanProperty
  var userId: Long = 0

  /**
   * 行程天数
   */
  @Min(value = 1)
  @BeanProperty
  var itineraryDays: Int = 0

  /**
   * 更新时间
   */
  @BeanProperty
  var updateTime: Instant = null

  /**
   * 攻略摘要
   */
  @BeanProperty
  var summary: String = null

  /**
   * 攻略详情
   */
  @BeanProperty
  var detailUrl: String = null

  /**
   * 可见度：public-所有人可见，private-自己可见
   */
  @BeanProperty
  var visibility: String = null

  /**
   * 状态：traveled-已走过的，planned-计划的
   */
  @BeanProperty
  var status: String = null
}
