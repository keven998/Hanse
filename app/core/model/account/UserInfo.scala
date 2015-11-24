package core.model.account

import javax.validation.constraints.{ Min, NotNull }

import com.lvxingpai.model.account.RealNameInfo
import core.model.BasicEntity
import core.model.misc.PhoneNumber
import org.hibernate.validator.constraints.{ Length, NotBlank }

import scala.beans.BeanProperty

/**
 * 用户数据
 *
 * Created by zephyre on 10/19/15.
 */
class UserInfo extends BasicEntity {
  /**
   * 用户ID
   */
  @NotNull
  @Min(value = 1)
  @BeanProperty
  var userId: Long = 0

  /**
   * 用户的电话号码
   */
  @BeanProperty
  var tel: PhoneNumber = null

  /**
   * 昵称
   */
  @NotBlank
  @Length(min = 1, max = 64)
  @BeanProperty
  var nickname: String = null

  /**
   * 头像
   */
  @BeanProperty
  var avatar: String = null

  /**
   * 性别
   */
  @BeanProperty
  var gender: String = null

  /**
   * 用户签名
   */
  @Length(max = 1024)
  @BeanProperty
  var signagure: String = null

  /**
   * 旅客信息
   */
  var travellers: Map[String, RealNameInfo] = _
}
