package com.lvxingpai.model.geo

import javax.validation.constraints.{ Min, NotNull }

import com.lvxingpai.model.mixin.{ GeoPointEnabled, ImagesEnabled, RankEnabled }
import org.hibernate.validator.constraints.NotBlank

import scala.beans.{ BeanProperty, BooleanBeanProperty }

/**
 * 城市信息
 *
 * Created by pengyt on 2015/10/19.
 */
class Locality extends GeoEntity with ImagesEnabled with GeoPointEnabled with RankEnabled {

  /**
   * 外部交通信息。每个entry都是一个tip，为HTML格式
   */
  @BeanProperty
  var remoteTraffic: Seq[DetailsEntry] = null

  /**
   * 内部交通信息。每个entry都是一个tip，为HTML格式
   */
  @BeanProperty
  var localTraffic: Seq[DetailsEntry] = null

  /**
   * 购物综述，HTML格式
   */
  @BeanProperty
  var shoppingIntro: String = null

  /**
   * 特产
   */
  @BeanProperty
  var commodities: Seq[DetailsEntry] = null

  /**
   * 美食综述，HTML格式
   */
  @BeanProperty
  var diningIntro: String = null

  /**
   * 特色菜式
   */
  @BeanProperty
  var cuisines: Seq[DetailsEntry] = null

  /**
   * 活动综述
   */
  @BeanProperty
  var activityIntro: String = null

  /**
   * 活动
   */
  @BeanProperty
  var activities: Seq[DetailsEntry] = null

  /**
   * 小贴士
   */
  @BeanProperty
  var tips: Seq[DetailsEntry] = null

  /**
   * 历史文化
   */
  @BeanProperty
  var geoHistory: Seq[DetailsEntry] = null

  /**
   * 城市亮点
   */
  @BeanProperty
  var specials: Seq[DetailsEntry] = null

  /**
   * 中文名称
   */
  @NotBlank
  @BeanProperty
  var zhName: String = null

  /**
   * 英文名称
   */
  @BeanProperty
  var enName: String = null

  /**
   * 别名
   */
  @BeanProperty
  var alias: Set[String] = Set()

  /**
   * 去过的人数
   */
  @Min(value = 0)
  @BeanProperty
  var visitCnt: Int = 0

  /**
   * 评论条数
   */
  @Min(value = 0)
  @BeanProperty
  var commentCnt: Int = 0

  /**
   * 收藏次数
   */
  @Min(value = 0)
  @BeanProperty
  var favorCnt: Int = 0

  /**
   * 热门程度
   */
  @Min(value = 0)
  @BeanProperty
  var hotness: Double = 0.0

  /**
   * 评分
   */
  @Min(value = 0)
  @BeanProperty
  var rating: Double = 0.0

  /**
   * 是否为境外目的地
   */
  @BooleanBeanProperty
  var abroad: Boolean = false

  /**
   * 所在国家
   */
  @NotNull
  @BeanProperty
  var country: Country = null

  /**
   * 父行政区
   */
  @BeanProperty
  var superAdm: Locality = null

  /**
   * 标签
   */
  @BeanProperty
  var tags: Set[String] = Set()

  /**
   * 简介
   */
  @BeanProperty
  var desc: String = null

  /**
   * 最佳旅行时间
   */
  @BeanProperty
  var travelMonth: String = null

  /**
   * 建议游玩时间
   */
  @BeanProperty
  var timeCostDesc: String = null

  /**
   * 建议游玩时间（单位为小时）
   */
  @Min(value = 0)
  @BeanProperty
  var timeCost: Int = 0
}
