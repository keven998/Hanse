package core.formatter.marketplace.product

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.marketplace.product.Commodity
import com.lvxingpai.model.marketplace.seller.Seller
import com.lvxingpai.model.misc.RichText

import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/11/3.
 */
class CommoditySerializer extends JsonSerializer[Commodity] {

  override def serialize(commodity: Commodity, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", commodity.id.toString)
    gen.writeNumberField("commodityId", commodity.commodityId)
    gen.writeStringField("title", commodity.title)
    gen.writeNumberField("rating", commodity.rating)
    gen.writeNumberField("salesVolume", commodity.salesVolume)
    gen.writeNumberField("marketPrice", commodity.marketPrice)
    gen.writeNumberField("price", commodity.price)

    // 商家
    gen.writeFieldName("seller")
    val seller = commodity.seller
    if (seller != null) {
      val retSeller = serializers.findValueSerializer(classOf[Seller], null)
      retSeller.serialize(seller, gen, serializers)
    }

    gen.writeFieldName("desc")
    val desc = commodity.desc
    if (desc != null) {
      val retDesc = serializers.findValueSerializer(classOf[RichText], null)
      retDesc.serialize(desc, gen, serializers)
    }

    // notice
    gen.writeFieldName("notice")
    gen.writeStartArray()
    val notice = commodity.notice
    if (notice.nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[RichText], null)
      for (n <- notice)
        ret.serialize(n, gen, serializers)
    }
    gen.writeEndArray()

    // refundPolicy
    gen.writeFieldName("refundPolicy")
    gen.writeStartArray()
    val refundPolicy = commodity.refundPolicy
    if (refundPolicy.nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[RichText], null)
      for (r <- refundPolicy)
        ret.serialize(r, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeFieldName("trafficInfo")
    val trafficInfo = commodity.trafficInfo
    if (trafficInfo != null) {
      val retTrafficInfo = serializers.findValueSerializer(classOf[RichText], null)
      retTrafficInfo.serialize(trafficInfo, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeEndObject()
  }
}