package core.formatter.marketplace.product

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import com.lvxingpai.model.marketplace.product.Pricing
import core.misc.Utils
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/11/4.
 */
class PricingSerializer extends JsonSerializer[Pricing] {

  override def serialize(pricing: Pricing, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeNumberField("price", Utils.getActualPrice(pricing.price))

    gen.writeFieldName("timeRange")
    gen.writeStartArray()
    if (pricing.timeRange != null) {
      for (t <- pricing.timeRange)
        gen.writeNumber(t.getTime)
    }
    gen.writeEndArray()

    gen.writeEndObject()
  }
}
