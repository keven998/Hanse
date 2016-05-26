package core.formatter.marketplace.seller

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.account.UserInfo
import com.lvxingpai.model.marketplace.seller.Seller

import scala.collection.JavaConversions._

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

    // 服务语言
    gen.writeFieldName("lang")
    gen.writeStartArray()
    Option(seller.lang) map (_.toSeq) getOrElse Seq() map {
      case "zh" => "中文"
      case "en" => "英文"
      case "local" => "当地语言"
    } foreach (gen writeString _)
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

    gen.writeEndObject()
  }
}