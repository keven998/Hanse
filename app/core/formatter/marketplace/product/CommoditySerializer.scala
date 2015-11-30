package core.formatter.marketplace.product

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.geo.Locality
import com.lvxingpai.model.marketplace.product.Commodity
import com.lvxingpai.model.marketplace.seller.Seller
import com.lvxingpai.model.misc.{ ImageItem, RichText }

import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/11/3.
 */
class CommoditySerializer extends JsonSerializer[Commodity] {

  override def serialize(commodity: Commodity, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    if (commodity.id != null)
      gen.writeStringField("id", commodity.id.toString)
    gen.writeNumberField("commodityId", Option(commodity.commodityId) getOrElse 0L)
    gen.writeStringField("title", Option(commodity.title) getOrElse "")
    gen.writeNumberField("rating", Option(commodity.rating) getOrElse 0.0d)
    gen.writeNumberField("salesVolume", Option(commodity.salesVolume) getOrElse 0)
    gen.writeNumberField("marketPrice", Option(commodity.marketPrice) getOrElse 0.0f)
    gen.writeNumberField("price", Option(commodity.price) getOrElse 0.0f)

    // categories
    gen.writeFieldName("category")
    gen.writeStartArray()
    val categories = commodity.category
    if (categories.nonEmpty) {
      for (category <- categories)
        gen.writeString(category)
    }
    gen.writeEndArray()

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

    // 商家
    gen.writeFieldName("seller")
    val seller = commodity.seller
    if (seller != null) {
      val retSeller = serializers.findValueSerializer(classOf[Seller], null)
      retSeller.serialize(seller, gen, serializers)
    }

    gen.writeFieldName("locality")
    val loc = commodity.locality
    if (loc != null) {
      val retSeller = serializers.findValueSerializer(classOf[Locality], null)
      retSeller.serialize(loc, gen, serializers)
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
    if (notice != null) {
      val ret = serializers.findValueSerializer(classOf[RichText], null)
      for (n <- notice)
        ret.serialize(n, gen, serializers)
    }
    gen.writeEndArray()

    // refundPolicy
    gen.writeFieldName("refundPolicy")
    gen.writeStartArray()
    val refundPolicy = commodity.refundPolicy
    if (refundPolicy != null) {
      val ret = serializers.findValueSerializer(classOf[RichText], null)
      for (r <- refundPolicy)
        ret.serialize(r, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeFieldName("trafficInfo")
    gen.writeStartArray()
    val trafficInfo = commodity.trafficInfo
    if (trafficInfo != null) {
      val ret = serializers.findValueSerializer(classOf[RichText], null)
      for (t <- trafficInfo)
        ret.serialize(t, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeEndObject()
  }
}