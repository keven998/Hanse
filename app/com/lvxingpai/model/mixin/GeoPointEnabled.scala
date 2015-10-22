package com.lvxingpai.model.mixin

import com.lvxingpai.model.geo.GeoPoint

import scala.beans.BeanProperty

/**
 * 经纬度数据
 *
 * Created by zephyre on 10/20/15.
 */
trait GeoPointEnabled {

  @BeanProperty
  var location: GeoPoint = null
}
