package core.formatter.marketplace.order

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.marketplace.product.Schedule
import com.lvxingpai.model.marketplace.seller.Seller
import core.misc.Utils

/**
 * Created by topy on 2016/3/30.
 */
class ScheduleSerializer extends JsonSerializer[Schedule] {

  override def serialize(schedule: Schedule, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeNumberField("itemId", schedule.itemId)
    gen.writeStringField("desc", schedule.desc)
    gen.writeNumberField("price", Utils.getActualPrice(schedule.price))
    gen.writeNumberField("bountyId", schedule.bountyId)
    gen.writeStringField("status", schedule.status)

    gen.writeFieldName("seller")
    val seller = schedule.seller
    if (seller != null) {
      val retSeller = serializers.findValueSerializer(classOf[Seller], null)
      retSeller.serialize(seller, gen, serializers)
    }

    gen.writeNumberField("createTime", if (schedule.createTime != null) schedule.createTime.getTime else 0)
    gen.writeNumberField("updateTime", if (schedule.updateTime != null) schedule.updateTime.getTime else 0)

    gen.writeEndObject()
  }
}
