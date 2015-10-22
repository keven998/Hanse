package com.lvxingpai.model.geo

import javax.validation.constraints.{ Max, Min }

import scala.beans.BeanProperty

/**
 * 描述一个地理意义上的点
 *
 * Created by pengyt on 2015/10/19.
 */
case class GeoPoint(@Min(value = -90)@Max(value = 90)@BeanProperty lat: Double,
  @Min(value = -180)@Max(value = 180)@BeanProperty lng: Double) extends GeoLocation

