package core.formatter.marketplace.order

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.account.{ RealNameInfo, UserInfo }
import com.lvxingpai.model.geo.Locality
import com.lvxingpai.model.marketplace.order.Bounty
import com.lvxingpai.model.marketplace.product.Schedule
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
    gen.writeStringField("status", bounty.status)

    gen.writeFieldName("consumer")
    val userInfo = bounty.consumer
    val retUserInfo = if (userInfo != null) serializers.findValueSerializer(classOf[UserInfo], null)
    else serializers.findNullValueSerializer(null)
    retUserInfo.serialize(userInfo, gen, serializers)

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

    gen.writeFieldName("departure")
    gen.writeStartArray()
    val loc = bounty.departure
    if (loc != null) {
      val retLoc = serializers.findValueSerializer(classOf[Locality], null)
      retLoc.serialize(loc, gen, serializers)
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
      val retTakers = serializers.findValueSerializer(classOf[UserInfo], null)
      for (pl <- takers)
        retTakers.serialize(pl, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeFieldName("scheduled")
    val scheduled = bounty.scheduled
    if (Option(scheduled).nonEmpty)
      serializers.findValueSerializer(classOf[Schedule], null).serialize(scheduled, gen, serializers)
    else {
      gen.writeStartObject()
      gen.writeEndObject()
    }

    gen.writeFieldName("schedules")
    gen.writeStartArray()
    val retSchedules = serializers.findValueSerializer(classOf[Schedule], null)
    Option(bounty.schedules) map (_.toSeq) getOrElse Seq() foreach (retSchedules.serialize(_, gen, serializers))
    gen.writeEndArray()

    //gen.writeBooleanField("paid", bounty.paid)
    // 是否已支付商家的行程安排
    gen.writeBooleanField("schedulePaid", bounty.schedulePaid)
    gen.writeNumberField("totalPrice", Utils.getActualPrice(bounty.totalPrice))
    // 是否已支付赏金
    gen.writeBooleanField("bountyPaid", bounty.bountyPaid)
    gen.writeNumberField("bountyPrice", Utils.getActualPrice(bounty.bountyPrice))

    gen.writeNumberField("createTime", if (bounty.createTime != null) bounty.createTime.getTime else 0)
    gen.writeNumberField("updateTime", if (bounty.updateTime != null) bounty.updateTime.getTime else 0)
    // 悬赏分享
    gen.writeStringField("shareURL", "http://h5.lvxingpai.com/task/task_detail.php?tid=" + bounty.itemId)

    gen.writeEndObject()
  }
}
