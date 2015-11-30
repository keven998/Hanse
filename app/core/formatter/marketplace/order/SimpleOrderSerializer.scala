package core.formatter.marketplace.order

import java.text.SimpleDateFormat

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.marketplace.order.Order
import com.lvxingpai.model.marketplace.product.Commodity

import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/11/21.
 */
class SimpleOrderSerializer extends JsonSerializer[Order] {

  override def serialize(order: Order, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    gen.writeNumberField("orderId", Option(order.orderId) getOrElse 0L)
    gen.writeNumberField("consumerId", Option(order.consumerId) getOrElse 0L)
    gen.writeStringField("planId", Option(order.planId) getOrElse "")

    // Commodity
    gen.writeFieldName("commodity")
    val commodity = order.commodity
    if (commodity != null) {
      val retSeller = serializers.findValueSerializer(classOf[Commodity], null)
      retSeller.serialize(commodity, gen, serializers)
    }

    // commodityTimeRange
    gen.writeFieldName("commodityTimeRange")
    gen.writeStartArray()
    val commodityTimeRange = order.commodityTimeRange
    if (commodityTimeRange != null) {
      for (c <- commodityTimeRange)
        gen.writeString(c.toString)
    }
    gen.writeEndArray()

    // 计算商品总价
    if (order.commodity != null && order.commodity.plans != null && order.commodity.plans.nonEmpty)
      order.totalPrice = order.commodity.plans.get(0).price * order.quantity
    else
      order.totalPrice = 0
    gen.writeNumberField("totalPrice", order.totalPrice)
    gen.writeNumberField("discount", Option(order.discount) getOrElse 0.0f)
    gen.writeNumberField("quantity", Option(order.quantity) getOrElse 0)

    //gen.writeStringField("comment", Option(order.comment) getOrElse "")
    gen.writeStringField("status", Option(order.status) getOrElse "")

    val fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    gen.writeStringField("rendezvousTime", if (order.rendezvousTime != null) fmt.format(order.rendezvousTime) else "")
    gen.writeStringField("createTime", if (order.rendezvousTime != null) fmt.format(order.createTime) else "")
    gen.writeStringField("updateTime", if (order.rendezvousTime != null) fmt.format(order.updateTime) else "")
    gen.writeStringField("expireDate", if (order.rendezvousTime != null) fmt.format(order.expireDate) else "")

    gen.writeEndObject()
  }
}