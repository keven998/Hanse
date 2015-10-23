package core.model.poi

import org.hibernate.validator.constraints.NotBlank

import scala.beans.BeanProperty

/**
 * POI描述
 * Created by pengyt on 2015/10/19.
 */
class Description {

  /**
   * 简略描述
   */
  @NotBlank
  @BeanProperty
  var desc: String = null

  /**
   * 描述详情
   */
  @BeanProperty
  var details: String = null

  /**
   * 贴士
   */
  @BeanProperty
  var tips: String = null

  /**
   * 交通信息
   */
  @BeanProperty
  var traffic: String = null
}