package com.lvxingpai.model.mixin

import javax.validation.constraints.Min

import scala.beans.BeanProperty

/**
 * 排名数据
 *
 * Created by zephyre on 10/20/15.
 */
trait RankEnabled {
  @Min(value = 1)
  @BeanProperty
  var rank: Int = 0

}
