package core.formatter.marketplace.order

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{ JsonSerializer, ObjectMapper, SerializerProvider }
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.account.{ RealNameInfo, UserInfo }
import com.lvxingpai.model.geo.Locality
import com.lvxingpai.model.marketplace.order.Bounty
import com.lvxingpai.model.marketplace.product.Schedule
import com.lvxingpai.model.marketplace.seller.Seller
import com.lvxingpai.model.misc.{ ImageItem, PhoneNumber, RichText }
import core.formatter.BaseFormatter
import core.formatter.geo.SimpleLocalitySerializer
import core.formatter.marketplace.seller.MiniSellerSerializer
import core.formatter.misc.{ ImageItemSerializer, PhoneNumberSerializer, RealNameInfoSerializer, RichTextSerializer }
import core.formatter.user.UserSerializer
import core.misc.Utils
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{ DateTime, DateTimeZone }

import scala.collection.JavaConversions._

/**
 * Created by topy on 2016/3/31.
 */
class SimpleBountyFormatter extends BaseFormatter {

  override protected val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    mapper.registerModule(DefaultScalaModule)
    module.addSerializer(classOf[Bounty], new SimpleBountySerializer)
    module.addSerializer(classOf[Seller], new MiniSellerSerializer)
    module.addSerializer(classOf[RichText], new RichTextSerializer)
    module.addSerializer(classOf[PhoneNumber], new PhoneNumberSerializer)
    module.addSerializer(classOf[UserInfo], new UserSerializer)
    module.addSerializer(classOf[ImageItem], new ImageItemSerializer)
    module.addSerializer(classOf[RealNameInfo], new RealNameInfoSerializer)
    module.addSerializer(classOf[Locality], new SimpleLocalitySerializer)
    mapper.registerModule(module)
    mapper
  }
}

object SimpleBountyFormatter {
  lazy val instance = new SimpleBountyFormatter
}

class SimpleBountySerializer extends JsonSerializer[Bounty] {

  override def serialize(bounty: Bounty, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeNumberField("itemId", bounty.itemId)
    gen.writeNumberField("consumerId", bounty.consumerId)

    gen.writeNumberField("takersCnt", (Option(bounty.takers) map (_.toSeq) getOrElse Seq()).size)

    gen.writeFieldName("destination")
    gen.writeStartArray()
    val plans = bounty.destination
    if (plans != null) {
      val ret = serializers.findValueSerializer(classOf[Locality], null)
      for (pl <- plans)
        ret.serialize(pl, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeNumberField("timeCost", bounty.timeCost)
    gen.writeNumberField("budget", bounty.budget)

    val departureDate = Option(bounty.departureDate) map (date => {
      val tzDate = new DateTime(date) toDateTime DateTimeZone.forID("Asia/Shanghai")
      val fmt = ISODateTimeFormat.date()
      tzDate.toString(fmt)
    }) getOrElse ""
    gen.writeStringField("departureDate", departureDate)

    gen.writeStringField("service", bounty.service)
    gen.writeStringField("topic", bounty.topic)

    //gen.writeBooleanField("paid", bounty.paid)
    // 是否已支付商家的行程安排
    gen.writeBooleanField("schedulePaid", bounty.schedulePaid)
    gen.writeNumberField("totalPrice", Utils.getActualPrice(bounty.totalPrice))
    // 是否已支付赏金
    gen.writeBooleanField("bountyPaid", bounty.bountyPaid)
    gen.writeNumberField("bountyPrice", Utils.getActualPrice(bounty.bountyPrice))

    gen.writeFieldName("scheduled")
    val scheduled = bounty.scheduled
    if (Option(scheduled).nonEmpty)
      serializers.findValueSerializer(classOf[Schedule], null).serialize(scheduled, gen, serializers)
    else {
      gen.writeStartObject()
      gen.writeEndObject()
    }

    gen.writeNumberField("createTime", if (bounty.createTime != null) bounty.createTime.getTime else 0)
    gen.writeNumberField("updateTime", if (bounty.updateTime != null) bounty.updateTime.getTime else 0)
    gen.writeEndObject()
  }
}

