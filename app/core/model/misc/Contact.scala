package core.model.misc

import java.util.{ List => JList }
import scala.beans.BeanProperty

/**
 * 联系信息
 * Created by pengyt on 2015/10/19.
 */
class Contact {

  /**
   * 电话号码列表: 010-83671111
   */
  @BeanProperty
  var phoneList: JList[String] = null

  /**
   * 手机号列表: 13811111111
   */
  @BeanProperty
  var cellphoneList: JList[String] = null

  /**
   * 传真
   */
  @BeanProperty
  var fax: String = null

  /**
   * 电子邮箱
   */
  @BeanProperty
  var email: String = null

  /**
   * 网址
   */
  @BeanProperty
  var website: String = null
}