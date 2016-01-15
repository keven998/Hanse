package core.formatter.marketplace.seller

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.geo.{ Locality, GeoEntity }
import com.lvxingpai.model.marketplace.seller.Seller
import com.lvxingpai.model.misc.{ ImageItem, RichText }

import scala.collection.JavaConversions._

/**
 * Created by topy on 2015/11/3.
 */
class SellerSerializer extends JsonSerializer[Seller] {

  override def serialize(seller: Seller, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    if (seller == null) {
      gen.writeEndObject()
      return
    }
    gen.writeStringField("id", seller.id.toString)

    gen.writeNumberField("sellerId", seller.sellerId)
    gen.writeStringField("name", seller.name)

    gen.writeFieldName("desc")
    val desc = seller.desc
    val retDesc = if (desc != null) serializers.findValueSerializer(classOf[RichText], null)
    else serializers.findNullValueSerializer(null)
    retDesc.serialize(desc, gen, serializers)

    // 服务语言
    gen.writeFieldName("lang")
    gen.writeStartArray()
    Option(seller.lang) map (_.toSeq) getOrElse Seq() map {
      case "zh" => "中文"
      case "en" => "英文"
      case "local" => "当地语言"
    } foreach (gen writeString _)
    gen.writeEndArray()

    // 商户资质
    gen.writeFieldName("qualifications")
    gen.writeStartArray()
    Option(seller.qualifications) map (_.toSeq) getOrElse Seq() foreach (gen writeString _)
    gen.writeEndArray()

    // 服务标签
    gen.writeFieldName("services")
    gen.writeStartArray()
    Option(seller.services) map (_.toSeq) getOrElse Seq() map {
      case "language" => "语言帮助"
      case "plan" => "行程规划"
      case "consult" => "当地咨询"
    } foreach (gen writeString _)
    gen.writeEndArray()

    // 服务区域，可以是国家，也可以是目的地
    gen.writeFieldName("serviceZones")
    gen.writeStartArray()
    val retServiceZones = serializers.findValueSerializer(classOf[GeoEntity], null)
    Option(seller.serviceZones) map (_.toSeq) filter (_.isInstanceOf[Locality]) getOrElse Seq() foreach
      (retServiceZones.serialize(_, gen, serializers))
    gen.writeEndArray()

    gen.writeStringField("address", Option(seller.address) getOrElse "")

    gen.writeNumberField("favorCnt", seller.favorCnt)
    gen.writeNumberField("rating", seller.rating)

    gen.writeFieldName("cover")
    val retCover = serializers.findValueSerializer(classOf[ImageItem], null)
    retCover.serialize(seller.cover, gen, serializers)

    gen.writeFieldName("images")
    gen.writeStartArray()
    val retImg = serializers.findValueSerializer(classOf[ImageItem], null)
    Option(seller.images) map (_.toSeq) getOrElse Seq() foreach (retImg.serialize(_, gen, serializers))
    gen.writeEndArray()

    gen.writeEndObject()
  }
}