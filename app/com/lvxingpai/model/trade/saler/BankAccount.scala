package com.lvxingpai.model.trade.saler

import org.hibernate.validator.constraints.{ Length, NotBlank }

import scala.beans.BeanProperty

/**
 * 银行卡设置
 *
 * Created by zephyre on 10/21/15.
 */
class P_BankAccount {
  /**
   * 银行的SWIFT代码
   */
  @NotBlank
  @Length(min = 8, max = 11)
  @BeanProperty
  var swift: String = null

  /**
   * 银行账户的代码。境内银行直接填写账户号码，境外银行填写IBAN代码
   */
  @NotBlank
  @Length(max = 34)
  @BeanProperty
  var accountNumber: String = null

  /**
   * 银行名称
   */
  @NotBlank
  @Length(max = 512)
  @BeanProperty
  var bankName: String = null

  /**
   * 支行/分行名称
   */
  @Length(max = 512)
  @BeanProperty
  var branchName: String = null

  /**
   * 持卡人姓名
   */
  @NotBlank
  @Length(min = 1, max = 256)
  @BeanProperty
  var cardHolder: String = null

  /**
   * 账单地址
   */
  @Length(min = 1, max = 1024)
  @BeanProperty
  var billingAddress: String = null
}
