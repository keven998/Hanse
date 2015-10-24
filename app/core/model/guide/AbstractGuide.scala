package core.model.guide

import core.model.BasicEntity
import core.model.mixin.{ LocalitiesEnabled, ImagesEnabled }
import core.model.poi.{ Restaurant, Shopping }
import org.hibernate.validator.constraints.NotBlank
import java.util.{ List => JList }
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
  var itinerary: JList[ItinerItem] = null

  /**
   * 攻略中的购物
   */
  @BeanProperty
  var shopping: JList[Shopping] = null

  /**
   * 攻略中的美食
   */
  @BeanProperty
  var restaurant: JList[Restaurant] = null
}
