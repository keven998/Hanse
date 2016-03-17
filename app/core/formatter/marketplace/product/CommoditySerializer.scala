package core.formatter.marketplace.product

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.geo.{ Country, Locality }
import com.lvxingpai.model.marketplace.product.{ CommodityPlan, Commodity }
import com.lvxingpai.model.marketplace.seller.Seller
import com.lvxingpai.model.misc.{ ImageItem, RichText }
import core.misc.Utils

import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/11/3.
 */
class CommoditySerializer extends JsonSerializer[Commodity] {

  override def serialize(commodity: Commodity, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    if (commodity.id != null)
      gen.writeStringField("id", commodity.id.toString)
    gen.writeNumberField("commodityId", commodity.commodityId)
    gen.writeStringField("title", Option(commodity.title) getOrElse "")
    gen.writeNumberField("rating", commodity.rating)
    gen.writeStringField("status", commodity.status)
    gen.writeNumberField("salesVolume", commodity.salesVolume)
    gen.writeNumberField("marketPrice", Utils.getActualPrice(commodity.marketPrice))
    gen.writeNumberField("price", Utils.getActualPrice(commodity.price))

    // categories
    gen.writeFieldName("category")
    gen.writeStartArray()
    val categories = commodity.category
    if (categories.nonEmpty) {
      for (category <- categories)
        gen.writeString(category)
    }
    gen.writeEndArray()

    gen.writeFieldName("plans")
    gen.writeStartArray()
    val plans = commodity.plans
    if (plans != null) {
      val ret = serializers.findValueSerializer(classOf[CommodityPlan], null)
      for (pl <- plans)
        ret.serialize(pl, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeFieldName("cover")
    val cover = commodity.cover
    val retCover = if (cover != null) serializers.findValueSerializer(classOf[ImageItem], null)
    else serializers.findNullValueSerializer(null)
    retCover.serialize(cover, gen, serializers)

    // images
    gen.writeFieldName("images")
    gen.writeStartArray()
    val images = Option(commodity.images) map (_.toSeq) getOrElse Seq()
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

    gen.writeFieldName("country")
    val country = commodity.country
    if (country != null) {
      val retSeller = serializers.findValueSerializer(classOf[Country], null)
      retSeller.serialize(country, gen, serializers)
    }

    gen.writeFieldName("locality")
    val loc = commodity.locality
    if (loc != null) {
      val retSeller = serializers.findValueSerializer(classOf[Locality], null)
      retSeller.serialize(loc, gen, serializers)
    }

    gen.writeStringField("address", Option(commodity.address) getOrElse "")
    gen.writeStringField("timeCost", Option(commodity.timeCost) getOrElse "")

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

    val cId = commodity.commodityId
    // 商品详情
    gen.writeStringField("descUrl", s"http://h5.lvxingpai.com/poi/item.php?pid=$cId&field=desc")
    // 提示详情
    gen.writeStringField("noticeUrl", s"http://h5.lvxingpai.com/poi/item.php?pid=$cId&field=notice")
    // 预订流程
    gen.writeStringField("refundPolicyUrl", s"http://h5.lvxingpai.com/poi/item.php?pid=$cId&field=refundPolicy")
    // 交通信息
    gen.writeStringField("trafficInfoUrl", s"http://h5.lvxingpai.com/poi/item.php?pid=$cId&field=trafficInfo")

    gen.writeEndObject()
  }
}