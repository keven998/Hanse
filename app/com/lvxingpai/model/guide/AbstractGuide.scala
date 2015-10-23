package com.lvxingpai.model.guide

import com.lvxingpai.model.BasicEntity
import com.lvxingpai.model.mixin.{ LocalitiesEnabled, ImagesEnabled }
import com.lvxingpai.model.poi.{ Restaurant, Shopping }
import org.hibernate.validator.constraints.NotBlank

import scala.beans.BeanProperty

/**
 * 抽象攻略
 * Created by pengyt on 2015/10/21.
 */
abstract class AbstractGuide extends BasicEntity with ImagesEnabled with LocalitiesEnabled {

  /**
   * 攻略标题
   */
  @NotBlank
  @BeanProperty
  var title: String = null

  /**
   * 攻略中去的poi
   */
  @BeanProperty
  var itinerary: Seq[ItinerItem] = Seq()

  /**
   * 攻略中的购物
   */
  @BeanProperty
  var shopping: Seq[Shopping] = Seq()

  /**
   * 攻略中的美食
   */
  @BeanProperty
  var restaurant: Seq[Restaurant] = Seq()
}
