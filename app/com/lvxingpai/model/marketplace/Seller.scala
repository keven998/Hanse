package com.lvxingpai.model.marketplace

import java.util.{List => JList}

import com.lvxingpai.model.geo.GeoEntity
import com.lvxingpai.model.misc.PhoneNumber
import org.hibernate.validator.constraints.{NotBlank, Email, Length}
import org.mongodb.morphia.annotations.Entity

import scala.beans.BeanProperty

/**
 * 商家信息
 *
 * Created by zephyre on 10/20/15.
 */
@Entity
class Seller {
  /**
   * 服务语言，目前只支持en, zh, local三种
   */
  @BeanProperty
  var lang: JList[String] = null

  /**
   * 服务区域，可以是国家，也可以是目的地
   */
  @BeanProperty
  var serviceZone: JList[GeoEntity] = null

  /**
   * 实名信息
   */
  @BeanProperty
  var realNameInfo: RealNameInfo = null

//  /**
//   * 银行账户信息
//   */
//  @BeanProperty
//  var bankAccounts: JList[BankAccount] = null

  /**
   * 店铺名称
   */
  @NotBlank
  @Length(min = 1, max = 128)
  @BeanProperty
  var shopTitle: String = null

  /**
   *
   */
  @Email
  @BeanProperty
  var email: String = null

  /**
   * 电话号码
   */
  @BeanProperty
  var contact: JList[PhoneNumber] = null

  /**
   * 详细地址
   */
  @Length(max = 1024)
  @BeanProperty
  var address: String = null
}

