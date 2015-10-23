package core.model.trade.saler

import javax.validation.constraints.NotNull

import core.model.geo.Country
import org.hibernate.validator.constraints.{ Length, NotBlank }
import java.util.{ List => JList }

import scala.beans.BeanProperty

/**
 * 用户的实名认证信息
 *
 * Created by zephyre on 10/21/15.
 */
class RealNameInfo {

  /**
   * 实名认证的状态
   */
  object VeriStatus {
    /**
     * 未经过实名认证
     */
    val Unverified = "unverified"

    /**
     * 已提交认证申请
     */
    val Applied = "applied"

    /**
     * 通过了实名认证
     */
    val Verified = "verified"

    /**
     * 实名认证被拒绝
     */
    val Declined = "declined"
  }

  /**
   * 姓
   */
  @NotBlank
  @Length(min = 1, max = 64)
  @BeanProperty
  var surname: String = null

  /**
   * 名
   */
  @NotBlank
  @Length(min = 1, max = 256)
  @BeanProperty
  var givenName: String = null

  /**
   * 国籍
   */
  @NotNull
  @BeanProperty
  var nationality: JList[Country] = null

  /**
   * 身份证件的集合
   */
  @BeanProperty
  var idSet: Set[IdProof] = null

  /**
   * 实名认证状态
   */
  @NotNull
  @BeanProperty
  var veriStatus: String = null

  /**
   * 实名认证信息更新时间
   */
  @NotNull
  @BeanProperty
  var updateTime: Long = 0
}
