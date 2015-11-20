package core.formatter.marketplace.product

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.marketplace.product.Commodity
import com.lvxingpai.model.misc.ImageItem
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/11/13.
 */
class SimpleCommoditySerializer extends JsonSerializer[Commodity] {

  override def serialize(commodity: Commodity, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    if (commodity.title != null)
      gen.writeStringField("title", commodity.title)
    if (commodity.marketPrice != null)
      gen.writeNumberField("marketPrice", commodity.marketPrice)
    if (commodity.price != null)
      gen.writeNumberField("price", commodity.price)
    if (commodity.rating != null)
      gen.writeNumberField("rating", commodity.rating)
    if (commodity.salesVolume != null)
      gen.writeNumberField("salesVolume", commodity.salesVolume)
    // images
    gen.writeFieldName("images")
    gen.writeStartArray()
    val images = commodity.images
    if (images.nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- images)
        ret.serialize(image, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeEndObject()
  }
}
