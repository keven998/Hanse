package core.formatter.marketplace.seller

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.account.UserInfo
import com.lvxingpai.model.marketplace.seller.Seller

/**
 * Created by topy on 2015/11/3.
 */
class MiniSellerSerializer extends JsonSerializer[Seller] {

  override def serialize(seller: Seller, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    gen.writeNumberField("sellerId", seller.sellerId)
    gen.writeStringField("name", Option(seller.name) getOrElse "")

    gen.writeFieldName("user")
    val userInfo = seller.userInfo
    val retUserInfo = if (userInfo != null) serializers.findValueSerializer(classOf[UserInfo], null)
    else serializers.findNullValueSerializer(null)
    retUserInfo.serialize(userInfo, gen, serializers)

    gen.writeEndObject()
  }
}