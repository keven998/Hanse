package core.formatter.marketplace.seller

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.account.UserInfo
import com.lvxingpai.model.marketplace.seller.Seller
import com.lvxingpai.model.misc.ImageItem

import scala.collection.JavaConversions._

/**
 * Created by topy on 2015/11/3.
 */
class SimpleSellerSerializer extends JsonSerializer[Seller] {

  override def serialize(seller: Seller, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    gen.writeNumberField("sellerId", seller.sellerId)
    gen.writeStringField("name", Option(seller.name) getOrElse "")
    gen.writeNumberField("rating", seller.rating)

    gen.writeFieldName("user")
    val userInfo = seller.userInfo
    val retUserInfo = if (userInfo != null) serializers.findValueSerializer(classOf[UserInfo], null)
    else serializers.findNullValueSerializer(null)
    retUserInfo.serialize(userInfo, gen, serializers)

    // 服务语言
    gen.writeFieldName("lang")
    gen.writeStartArray()
    Option(seller.lang) map (_.toSeq) getOrElse Seq() foreach (gen writeString _)
    gen.writeEndArray()

    // 商户资质
    gen.writeFieldName("qualifications")
    gen.writeStartArray()
    Option(seller.qualifications) map (_.toSeq) getOrElse Seq() foreach (gen writeString _)
    gen.writeEndArray()

    // 服务标签
    gen.writeFieldName("services")
    gen.writeStartArray()
    Option(seller.services) map (_.toSeq) getOrElse Seq() foreach (gen writeString _)
    gen.writeEndArray()

    gen.writeFieldName("cover")
    val cover = seller.cover
    if (cover != null) {
      val retCover = serializers.findValueSerializer(classOf[ImageItem], null)
      //else serializers.findNullValueSerializer(null)
      retCover.serialize(cover, gen, serializers)
    } else {
      gen.writeStartObject()
      gen.writeEndObject()
    }

    gen.writeEndObject()
  }
}