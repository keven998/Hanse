package core.formatter.marketplace.product

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}
import com.lvxingpai.model.marketplace.product.Commodity

/**
 * Created by pengyt on 2015/11/3.
 */
class CommoditySerializer extends JsonSerializer[Commodity] {

  override def serialize(commodity: Commodity, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeNumberField("id", commodity.id)

    // 商家

    gen.writeStringField("title", commodity.title)

    // 商品套餐


    gen.writeEndObject()
  }
}