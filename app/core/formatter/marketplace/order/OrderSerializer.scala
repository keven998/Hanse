package core.formatter.marketplace.order

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.account.RealNameInfo
import com.lvxingpai.model.marketplace.order.{ Order, Prepay }
import com.lvxingpai.model.marketplace.product.Commodity

import scala.collection.JavaConversions._
/**
 * Created by pengyt on 2015/11/21.
 */
class OrderSerializer extends JsonSerializer[Order] {

  override def serialize(order: Order, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    gen.writeStringField("id", Option(order.id.toString) getOrElse "")
    gen.writeNumberField("orderId", Option(order.orderId) getOrElse 0L)
    gen.writeNumberField("consumerId", Option(order.consumerId) getOrElse 0L)

    // Commodity
    gen.writeFieldName("commodity")
    val commodity = order.commodity
    if (commodity != null) {
      val retSeller = serializers.findValueSerializer(classOf[Commodity], null)
      retSeller.serialize(commodity, gen, serializers)
    }

    gen.writeStringField("planId", Option(order.planId) getOrElse "")

    // travellers
    gen.writeFieldName("travellers")
    gen.writeStartArray()
    val travellers = order.travellers
    if (travellers != null) {
      val ret = serializers.findValueSerializer(classOf[RealNameInfo], null)
      for (traveller <- travellers)
        ret.serialize(traveller, gen, serializers)
    }
    gen.writeEndArray()

    // contact
    gen.writeFieldName("commodity")
    val contact = order.contact
    if (contact != null) {
      val retSeller = serializers.findValueSerializer(classOf[RealNameInfo], null)
      retSeller.serialize(contact, gen, serializers)
    }

    gen.writeStringField("rendezvousTime", Option(order.rendezvousTime.toString) getOrElse "")
    gen.writeStringField("createTime", Option(order.createTime.toString) getOrElse "")
    gen.writeStringField("updateTime", Option(order.updateTime.toString) getOrElse "")
    gen.writeStringField("expireDate", Option(order.expireDate.toString) getOrElse "")

    // commodityTimeRange
    gen.writeFieldName("commodityTimeRange")
    gen.writeStartArray()
    val commodityTimeRange = order.commodityTimeRange
    if (commodityTimeRange != null) {
      for (c <- commodityTimeRange)
        gen.writeString(c.toString)
    }
    gen.writeEndArray()

    gen.writeNumberField("totalPrice", Option(order.totalPrice) getOrElse 0.0f)
    gen.writeNumberField("discount", Option(order.discount) getOrElse 0.0f)
    gen.writeNumberField("quantity", Option(order.quantity) getOrElse 0)

    // paymentInfo
    gen.writeFieldName("paymentInfo")
    val paymentInfo = order.paymentInfo
    if (paymentInfo.nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[Prepay], null)
      for (payment <- paymentInfo) {
        gen.writeFieldName(payment._1)
        ret.serialize(payment._2, gen, serializers)
      }
    }

    gen.writeStringField("status", Option(order.status) getOrElse "")
    gen.writeStringField("comment", Option(order.comment) getOrElse "")
    gen.writeEndObject()
  }
}