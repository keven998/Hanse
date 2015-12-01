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

    gen.writeFieldName("user")
    val userInfo = seller.userInfo
    val retUserInfo = if (userInfo != null) serializers.findValueSerializer(classOf[UserInfo], null)
    else serializers.findNullValueSerializer(null)
    retUserInfo.serialize(userInfo, gen, serializers)

    // 商户资质
    gen.writeFieldName("qualifications")
    gen.writeStartArray()
    if (seller.qualifications != null) {
      for (l: String <- seller.qualifications)
        gen.writeString(l)
    }
    gen.writeEndArray()

    gen.writeNumberField("rating", Option(seller.rating) getOrElse 0.0d)

    gen.writeFieldName("cover")
    val cover = seller.cover
    val retCover = if (cover != null) serializers.findValueSerializer(classOf[ImageItem], null)
    else serializers.findNullValueSerializer(null)
    retCover.serialize(cover, gen, serializers)

    gen.writeEndObject()
  }
}