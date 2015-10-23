package core.model.misc

import javax.validation.constraints.Min

import core.model.geo.{ Country, Locality }

import scala.beans.BeanProperty

/**
 * 足迹
 * Created by pengyt on 2015/10/21.
 */
class Track {

  /**
   * 用户ID
   */
  @Min(value = 1)
  @BeanProperty
  var userId: Long = 0

  /**
   * 足迹所属的城市
   */
  @BeanProperty
  var locality: Locality = null

  /**
   * 足迹所属的国家
   */
  @BeanProperty
  var country: Country = null

}
