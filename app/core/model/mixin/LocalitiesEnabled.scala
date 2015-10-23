package core.model.mixin

import core.model.geo.Locality

import scala.beans.BeanProperty

/**
 * 目的地列表
 * Created by pengyt on 2015/10/21.
 */
trait LocalitiesEnabled {
  @BeanProperty
  var localities: Seq[Locality] = Seq()
}
