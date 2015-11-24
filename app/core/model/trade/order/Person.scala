package core.model.trade.order

import java.util.Date

import com.lvxingpai.model.account.{ IdProof, Gender }
import com.lvxingpai.model.misc.PhoneNumber
import org.hibernate.validator.constraints.{ Email, Length, NotBlank }
import org.mongodb.morphia.annotations.Embedded

import scala.beans.BeanProperty

@Embedded
class Person {

  /**
   * 姓
   */
  @NotBlank
  @Length(min = 1, max = 64)
  @BeanProperty
  var surname: String = _

  /**
   * 姓的拼音
   */
  var surnamePinyin: String = _

  /**
   * 名
   */
  @NotBlank
  @Length(min = 1, max = 256)
  @BeanProperty
  var givenName: String = _

  /**
   * 名的拼音
   */
  var givenNamePinyin: String = _

  /**
   * 性别
   */
  var gender: Gender.Value = _

  /**
   * 出生日期
   */
  var birthday: Date = _

  /**
   * 全名
   */
  var fullName: String = _

  /**
   * 身份信息
   */
  var idProof: IdProof = _

  /**
   * 联系电话
   */
  var tel: PhoneNumber = _

  /**
   * 联系邮箱
   */
  @Email
  var email: String = _
}
