package core.model.trade.saler

import javax.validation.constraints.NotNull

import core.model.account.UserInfo
import core.model.geo.GeoEntity
import core.model.misc.PhoneNumber
import org.hibernate.validator.constraints.{ Email, Length, NotBlank }
import java.util.{ List => JList }

import scala.beans.BeanProperty

/**
 * 商家信息
 *
 * Created by zephyre on 10/20/15.
 */
class Saler extends UserInfo {
  /**
   * 服务语言
   */
  @BeanProperty
  var lang: JList[String] = null

  /**
   * 服务区域，可以是国家，也可以是目的地
   */
  @NotNull
  @BeanProperty
  var serviceZone: JList[GeoEntity] = null

  /**
   * 实名信息
   */
  @NotNull
  @BeanProperty
  var realNameInfo: RealNameInfo = null

  /**
   * 银行账户信息
   */
  @BeanProperty
  var bankAccounts: JList[BankAccount] = null

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
  var contact: PhoneNumber = null

  /**
   * 详细地址
   */
  @Length(max = 1024)
  @BeanProperty
  var address: String = null
}
