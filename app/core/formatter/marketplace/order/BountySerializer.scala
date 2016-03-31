package core.formatter.marketplace.order

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.account.RealNameInfo
import com.lvxingpai.model.geo.Locality
import com.lvxingpai.model.marketplace.order.Bounty
import com.lvxingpai.yunkai.UserInfo
import core.misc.Utils
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{ DateTime, DateTimeZone }

import scala.collection.JavaConversions._

/**
 * Created by topy on 2016/3/30.
 */
class BountySerializer extends JsonSerializer[Bounty] {

  override def serialize(bounty: Bounty, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeNumberField("itemId", bounty.itemId)
    gen.writeNumberField("consumerId", bounty.consumerId)

    gen.writeFieldName("destination")
    gen.writeStartArray()
    val plans = bounty.destination
    if (plans != null) {
      val ret = serializers.findValueSerializer(classOf[Locality], null)
      for (pl <- plans)
        ret.serialize(pl, gen, serializers)
    }
    gen.writeEndArray()

    // contact
    gen.writeFieldName("contact")
    gen.writeStartArray()
    val contact = bounty.contact
    if (contact != null) {
      val retSeller = serializers.findValueSerializer(classOf[RealNameInfo], null)
      retSeller.serialize(contact, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeNumberField("totalPrice", Utils.getActualPrice(bounty.totalPrice))

    gen.writeFieldName("departure")
    gen.writeStartArray()
    val loc = bounty.departure
    if (loc != null) {
      val retSeller = serializers.findValueSerializer(classOf[Locality], null)
      retSeller.serialize(loc, gen, serializers)
    }
    gen.writeEndArray()

    val departureDate = Option(bounty.departureDate) map (date => {
      val tzDate = new DateTime(date) toDateTime DateTimeZone.forID("Asia/Shanghai")
      val fmt = ISODateTimeFormat.date()
      tzDate.toString(fmt)
    }) getOrElse ""
    gen.writeStringField("departureDate", departureDate)

    gen.writeNumberField("timeCost", bounty.timeCost)

    gen.writeFieldName("participants")
    gen.writeStartArray()
    val participants = bounty.participants
    if (participants != null) {
      for (participant <- participants)
        gen.writeString(participant)
    }
    gen.writeEndArray()

    gen.writeNumberField("participantCnt", bounty.participantCnt)
    gen.writeNumberField("budget", bounty.budget)
    gen.writeStringField("memo", bounty.memo)
    gen.writeStringField("service", bounty.service)
    gen.writeStringField("topic", bounty.topic)

    gen.writeFieldName("takers")
    gen.writeStartArray()
    val takers = bounty.takers
    if (takers != null) {
      val retSeller = serializers.findValueSerializer(classOf[UserInfo], null)
      retSeller.serialize(contact, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeBooleanField("paid", bounty.paid)
    gen.writeEndObject()
  }
}
