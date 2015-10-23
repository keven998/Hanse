package com.lvxingpai.model.trade.order

import java.beans.Transient

import com.lvxingpai.model.trade.order.{ P_Order => BasicOrder }
import org.bson.types.ObjectId

/**
 * 订单
 *
 * Created by topy on 10/22/15.
 */
class Order extends BasicOrder {

  var _id: ObjectId = null

}

object Order {

  @Transient
  var FD_ID: String = "id"

  @Transient
  var FD_COMMODITY: String = "commodity"
}
