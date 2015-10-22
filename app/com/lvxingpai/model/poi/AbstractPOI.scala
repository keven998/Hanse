package com.lvxingpai.model.poi

import com.lvxingpai.model.BasicEntity
import com.lvxingpai.model.geo._
import com.lvxingpai.model.misc.Contact
import com.lvxingpai.model.mixin._
import org.hibernate.validator.constraints.NotBlank

import scala.beans.{BeanProperty, BooleanBeanProperty}

/**
 * Created by pengyt on 2015/10/19.
 */
class AbstractPOI extends BasicEntity with GeoPointEnabled with ImagesEnabled with RankEnabled with RatingEnabled with HotnessEnabled {

  /**
   * POI联系信息
   */
  @BeanProperty
  var contact: Contact = null

  /**
   * 是否位于国外
   */
  @NotBlank
  @BooleanBeanProperty
  var abroad: Boolean = false

  /**
   * POI中文名
   */
  @NotBlank
  @BeanProperty
  var zhName: String = null

  /**
   * POI英文名
   */
  @NotBlank
  @BeanProperty
  var enName: String = null

  /**
   * POI链接
   */
  @NotBlank
  @BeanProperty
  var url: String = null

  /**
   * POI价格
   */
  @NotBlank
  @BeanProperty
  var price: Double = 0.0

  /**
   * POI价格描述
   */
  @BeanProperty
  var priceDesc: String = null

  /**
   * POI描述
   */
  @BeanProperty
  var desc: String = null

  /**
   * 开放时间描述
   */
  @BeanProperty
  var openTime: String = null

  /**
   * POI描述
   */
  var description: Description = null

  /**
   * POI标签
   */
  @BeanProperty
  var tags: Set[String] = Set()

  /**
   * POI的别名
   */
  @BeanProperty
  var alias: Set[String] = Set()

  /**
   * POI所在的行政区划。
   */
  @BeanProperty
  var targets: Seq[String] = Seq()

  /**
   * 表示该POI的来源。注意：一个POI可以有多个来源。
   * 示例：
   * <p>
   * source: { "baidu": {"url": "foobar", "id": 27384}}
   */
  var source: Map[String, AnyRef] = null

  /**
   * 旅行指南URL
   */
  @BeanProperty
  var guideUrl: String = null

  /**
   * POI地址
   */
  @NotBlank
  @BeanProperty
  var address: String = null

  /**
   * POI所属国家
   */
  @NotBlank
  @BeanProperty
  val country: Country = null

  /**
   * 从属行政关系
   */
  @BeanProperty
  var locList: Seq[Locality] = Seq()

  /**
   * 所在目的地
   */
  @BeanProperty
  var locality: Locality = null

}