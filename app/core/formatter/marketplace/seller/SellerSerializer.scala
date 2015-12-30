package core.formatter.marketplace.seller

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.geo.GeoEntity
import com.lvxingpai.model.marketplace.seller.Seller
import com.lvxingpai.model.misc.{ ImageItem, RichText }

import scala.collection.JavaConversions._

/**
 * Created by topy on 2015/11/3.
 */
class SellerSerializer extends JsonSerializer[Seller] {

  override def serialize(seller: Seller, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    gen.writeStringField("id", seller.id.toString)

    gen.writeNumberField("sellerId", seller.sellerId)
    gen.writeStringField("name", seller.name)

    gen.writeFieldName("desc")
    val desc = seller.desc
    val retDesc = if (desc != null) serializers.findValueSerializer(classOf[RichText], null)
    else serializers.findNullValueSerializer(null)
    retDesc.serialize(desc, gen, serializers)

    //    gen.writeFieldName("user")
    //    val userInfo = seller.userInfo
    //    val retUserInfo = if (userInfo != null) serializers.findValueSerializer(classOf[UserInfo], null)
    //    else serializers.findNullValueSerializer(null)
    //    retUserInfo.serialize(userInfo, gen, serializers)

    gen.writeFieldName("lang")
    gen.writeStartArray()
    if (seller.lang != null) {
      for (l: String <- seller.lang)
        gen.writeString(l)
    }
    gen.writeEndArray()

    // 商户资质
    gen.writeFieldName("qualifications")
    gen.writeStartArray()
    if (seller.qualifications != null) {
      for (l: String <- seller.qualifications)
        gen.writeString(l)
    }
    gen.writeEndArray()

    // 服务区域，可以是国家，也可以是目的地
    gen.writeFieldName("services")
    gen.writeStartArray()
    if (seller.services != null) {
      for (l: String <- seller.services)
        gen.writeString(l)
    }
    gen.writeEndArray()

    // 服务区域，可以是国家，也可以是目的地
    gen.writeFieldName("serviceZones")
    gen.writeStartArray()
    val serviceZones = seller.serviceZones
    if (serviceZones != null && !serviceZones.isEmpty) {
      val retServiceZones = serializers.findValueSerializer(classOf[GeoEntity], null)
      for (serviceZone <- serviceZones) {
        retServiceZones.serialize(serviceZone, gen, serializers)
      }
    }
    gen.writeEndArray()

    //    gen.writeFieldName("bankAccounts")
    //    gen.writeStartArray()
    //    val bankAccounts = seller.bankAccounts
    //    if (bankAccounts != null && !bankAccounts.isEmpty) {
    //      val retBankAccounts = serializers.findValueSerializer(classOf[BankAccount], null)
    //      for (bankAccount <- bankAccounts) {
    //        retBankAccounts.serialize(bankAccount, gen, serializers)
    //      }
    //    }
    //    gen.writeEndArray()
    //

    //    gen.writeFieldName("phone")
    //    gen.writeStartArray()
    //    val phone = seller.phone
    //    if (phone != null && !phone.isEmpty) {
    //      val retPhone = serializers.findValueSerializer(classOf[PhoneNumber], null)
    //      for (p <- phone) {
    //        retPhone.serialize(p, gen, serializers)
    //      }
    //    }
    //    gen.writeEndArray()

    gen.writeStringField("address", Some(seller.address) getOrElse "")

    gen.writeNumberField("favorCnt", seller.favorCnt)
    gen.writeNumberField("rating", seller.rating)

    gen.writeFieldName("cover")
    val cover = seller.cover
    if (cover != null) {
      val retCover = serializers.findValueSerializer(classOf[ImageItem], null)
      retCover.serialize(cover, gen, serializers)
    } else {
      gen.writeStartObject()
      gen.writeEndObject()
    }

    gen.writeFieldName("images")
    gen.writeStartArray()

    if (seller.images != null) {
      val retImg = serializers.findValueSerializer(classOf[ImageItem], null)
      for (i <- seller.images)
        retImg.serialize(i, gen, serializers)
    }
    gen.writeEndArray()

    //    gen.writeFieldName("email")
    //    gen.writeStartArray()
    //    if (seller.email != null) {
    //      for (e <- seller.email)
    //        gen.writeString(e)
    //    }
    //    gen.writeEndArray()

    gen.writeEndObject()
  }
}