package com.lvxingpai.model.misc

import org.mongodb.morphia.annotations.Embedded

/**
 * 电话号码，参考：com.google.i18n.phonenumbers
 *
 * Created by zephyre on 10/27/15.
 */
@Embedded
class PhoneNumber {
  /**
   * 国家代码
   */
  var countryCode:Int=86

  /**
   * 电话号码
   */
  var nationalNumber:Long=0
}
