package com.lvxingpai.model.misc

import scala.beans.BeanProperty

/**
 * 热门搜索
 * Created by pengyt on 2015/10/21.
 */
class HotSearch {

  /**
   * 搜索类型
   */
  @BeanProperty
  var searchType: String = null

  /**
   * 要搜索的域
   */
  @BeanProperty
  var searchField: String = null

  /**
   * 搜索项的名称
   */
  @BeanProperty
  var itemName: String = null

  /**
   * 项的Id, Object的字符串形式
   */
  @BeanProperty
  var itemId: String = null
}
