package core.formatter.marketplace.order

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.marketplace.order.OrderActivity
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/11/21.
 */
class OrderActivitySerializer extends JsonSerializer[OrderActivity] {

  override def serialize(act: OrderActivity, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    if (act == null) {
      gen.writeEndObject()
      return
    }

    gen.writeStringField("action", Option(act.action) getOrElse "")

    gen.writeStringField("prevStatus", Option(act.prevStatus) getOrElse "")

    //    gen.writeFieldName("data")
    //    val commodity = act.data
    //    if (commodity != null) {
    //      val retSeller = serializers.findValueSerializer(classOf[HashMap], null)
    //      retSeller.serialize(commodity, gen, serializers)
    //    }

    gen.writeFieldName("data")

    val data = act.data
    gen.writeStartObject()
    if (data != null) {
      data foreach (entry => {
        gen.writeStringField(entry._1, entry._2.toString)
      })
    }
    gen.writeEndObject()

    //foreach (gen.writeStringField("prevStatus", Option(act.prevStatus) getOrElse ""))

    gen.writeNumberField("timestamp", if (act.timestamp != null) act.timestamp.getTime else 0)

    gen.writeEndObject()
  }
}