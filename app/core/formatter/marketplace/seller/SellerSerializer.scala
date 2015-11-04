package core.formatter.marketplace.seller

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{JsonSerializer, SerializerProvider}
import com.lvxingpai.model.marketplace.seller.Seller

/**
 * Created by pengyt on 2015/11/3.
 */
class SellerSerializer extends JsonSerializer[Seller] {

  override def serialize(seller: Seller, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()




    gen.writeEndObject()
  }
}