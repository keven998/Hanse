package com.lvxingpai.model.misc

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
  var phoneList: Set[String] = Set()

  /**
   * 手机号列表: 13811111111
   */
  @BeanProperty
  var cellphoneList: Set[String] = Set()

  /**
   * 传真
   */
  @BeanProperty
  var fax: Option[String] = None

  /**
   * 电子邮箱
   */
  @BeanProperty
  var email: Option[String] = None

  /**
   * 网址
   */
  @BeanProperty
  var website: Option[String] = None
}