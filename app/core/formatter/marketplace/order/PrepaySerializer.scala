package core.formatter.marketplace.order

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.marketplace.order.Prepay

/**
 * Created by pengyt on 2015/11/21.
 */
class PrepaySerializer extends JsonSerializer[Prepay] {

  override def serialize(prepay: Prepay, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    //    gen.writeStringField("id", Option(prepay.id.toString) getOrElse "")
    //    gen.writeStringField("provider", Option(prepay.provider) getOrElse "")
    //    gen.writeStringField("prepayId", Option(prepay.prepayId) getOrElse "")
    //    gen.writeNumberField("amount", Option(prepay.amount) getOrElse 0.0f)
    //    //gen.writeStringField("timestamp", Option(prepay.timestamp.toString) getOrElse "")
    //    gen.writeStringField("nonceString", Option(prepay.nonceString) getOrElse "")
    //    gen.writeStringField("sign", Option(prepay.sign) getOrElse "")
    //    gen.writeStringField("tradeType", Option(prepay.tradeType) getOrElse "")
    //    gen.writeStringField("vendor", Option(prepay.vendor) getOrElse "")
    //    gen.writeStringField("result", Option(prepay.result) getOrElse "")
    gen.writeEndObject()
  }
}