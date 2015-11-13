package core.formatter.marketplace.product

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.marketplace.product.Commodity

/**
 * Created by pengyt on 2015/11/13.
 */
class SimpleCommoditySerializer extends JsonSerializer[Commodity] {

  override def serialize(commodity: Commodity, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("title", commodity.title)
    gen.writeNumberField("marketPrice", commodity.marketPrice)
    gen.writeNumberField("price", commodity.price)
    gen.writeNumberField("rating", commodity.rating)
    gen.writeNumberField("salesVolume", commodity.salesVolume)

    gen.writeEndObject()
  }
}
