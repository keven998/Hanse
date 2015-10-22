package com.lvxingpai.model.trade.saler

import javax.validation.constraints.NotNull

import com.lvxingpai.model.geo.Country
import org.hibernate.validator.constraints.{ Length, NotBlank }
import org.joda.time.Instant

import scala.beans.BeanProperty

/**
 * 用户的实名认证信息
 *
 * Created by zephyre on 10/21/15.
 */
class P_RealNameInfo {

  /**
   * 实名认证的状态
   */
  object VeriStatus extends Enumeration {
    /**
     * 未经过实名认证
     */
    val Unverified = Value("unverified")

    /**
     * 已提交认证申请
     */
    val Applied = Value("applied")

    /**
     * 通过了实名认证
     */
    val Verified = Value("verified")

    /**
     * 实名认证被拒绝
     */
    val Declined = Value("declined")
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
  var nationality: Set[Country] = null

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
  var veriStatus: VeriStatus.Value = null

  /**
   * 实名认证信息更新时间
   */
  @NotNull
  @BeanProperty
  var updateTime: Instant = null
}
