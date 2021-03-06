package core.formatter.marketplace.order

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.account.RealNameInfo
import com.lvxingpai.model.marketplace.order.{ Order, OrderActivity }
import com.lvxingpai.model.marketplace.product.Commodity
import core.misc.Utils
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{ DateTimeZone, DateTime }

import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/11/21.
 */
class OrderSerializer extends JsonSerializer[Order] {

  override def serialize(order: Order, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    //gen.writeStringField("id", Option(order.id.toString) getOrElse "")
    gen.writeNumberField("orderId", order.orderId)
    gen.writeNumberField("consumerId", order.consumerId)
    gen.writeStringField("planId", order.planId)

    // Commodity
    gen.writeFieldName("commodity")
    val commodity = order.commodity
    if (commodity != null) {
      val retSeller = serializers.findValueSerializer(classOf[Commodity], null)
      retSeller.serialize(commodity, gen, serializers)
    }

    // travellers
    gen.writeFieldName("travellers")
    gen.writeStartArray()
    val travellers = order.travellers
    if (travellers != null) {
      val ret = serializers.findValueSerializer(classOf[RealNameInfo], null)
      for (traveller <- travellers)
        ret.serialize(traveller, gen, serializers)
    } else serializers.findNullValueSerializer(null)
    gen.writeEndArray()

    // contact
    gen.writeFieldName("contact")
    val contact = order.contact
    if (contact != null) {
      val retSeller = serializers.findValueSerializer(classOf[RealNameInfo], null)
      retSeller.serialize(contact, gen, serializers)
    }

    // commodityTimeRange
    //    gen.writeFieldName("commodityTimeRange")
    //    gen.writeStartArray()
    //    val commodityTimeRange = order.commodityTimeRange
    //    if (commodityTimeRange != null) {
    //      for (c <- commodityTimeRange)
    //        gen.writeString(c.toString)
    //    }
    //    gen.writeEndArray()

    gen.writeNumberField("totalPrice", Utils.getActualPrice(order.totalPrice))
    gen.writeNumberField("discount", Utils.getActualPrice(order.discount))
    gen.writeNumberField("quantity", order.quantity)

    // paymentInfo
    //    gen.writeFieldName("paymentInfo")
    //    val paymentInfo = order.paymentInfo
    //    if (paymentInfo.nonEmpty) {
    //      val ret = serializers.findValueSerializer(classOf[Prepay], null)
    //      for (payment <- paymentInfo) {
    //        gen.writeFieldName(payment._1)
    //        ret.serialize(payment._2, gen, serializers)
    //      }
    //    }
    gen.writeStringField("comment", Option(order.comment) getOrElse "")
    gen.writeStringField("status", Option(order.status) getOrElse "")

    gen.writeFieldName("activities")
    gen.writeStartArray()
    val orderAct = serializers.findValueSerializer(classOf[OrderActivity], null)
    Option(order.activities) map (_.toSeq) getOrElse Seq() foreach (orderAct.serialize(_, gen, serializers))
    gen.writeEndArray()

    // 是否已发货
    val commit = Option(order.activities) map (_.toSeq) getOrElse Seq() filter (_.action equals OrderActivity.Action.commit.toString)
    gen.writeBooleanField("committed", commit.size > 0)

    //  val fmt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
    //    gen.writeStringField("rendezvousTime", if (order.rendezvousTime != null) fmt.format(order.rendezvousTime) else "")
    val rendezvous = Option(order.rendezvousTime) map (date => {
      val tzDate = new DateTime(date) toDateTime DateTimeZone.forID("Asia/Shanghai")
      val fmt = ISODateTimeFormat.date()
      tzDate.toString(fmt)
    }) getOrElse ""
    gen.writeStringField("rendezvousTime", rendezvous)
    gen.writeNumberField("createTime", if (order.createTime != null) order.createTime.getTime else 0)
    gen.writeNumberField("updateTime", if (order.updateTime != null) order.updateTime.getTime else 0)
    gen.writeNumberField("expireTime", if (order.expireDate != null) order.expireDate.getTime else 0)
    gen.writeEndObject()
  }
}