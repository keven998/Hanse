package com.lvxingpai.model.trade.saler

import javax.validation.constraints.Pattern

import org.hibernate.validator.constraints.NotBlank

import scala.beans.BeanProperty

/**
 * 由中国大陆颁发的身份证件
 *
 * Created by zephyre on 10/21/15.
 */
class P_ChineseID extends IdProof {
  /**
   * 身份证号码（15位或者18位）
   */
  @NotBlank
  @Pattern(regexp = "([\\d]{17}[\\dX]|[\\d]{15})")
  @BeanProperty
  var number: String = null
}
