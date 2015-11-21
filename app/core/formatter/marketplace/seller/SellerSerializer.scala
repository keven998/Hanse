package core.formatter.marketplace.seller

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.geo.GeoEntity
import com.lvxingpai.model.marketplace.seller.{ Seller, BankAccount }
import com.lvxingpai.model.misc.{ RichText, PhoneNumber }
import com.lvxingpai.yunkai.UserInfo

import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/11/3.
 */
class SellerSerializer extends JsonSerializer[Seller] {

  override def serialize(seller: Seller, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeNumberField("sellerId", seller.sellerId)
    gen.writeStringField("name", Option(seller.name) getOrElse "")

    gen.writeFieldName("desc")
    val desc = seller.desc
    if (desc != null) {
      val retDesc = serializers.findValueSerializer(classOf[RichText], null)
      retDesc.serialize(desc, gen, serializers)
    }

    gen.writeFieldName("user")
    val userInfo = seller.userInfo
    if (userInfo != null) {
      val retIdProof = serializers.findValueSerializer(classOf[UserInfo], null)
      retIdProof.serialize(userInfo, gen, serializers)
    }

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

    gen.writeFieldName("bankAccounts")
    gen.writeStartArray()
    val bankAccounts = seller.bankAccounts
    if (bankAccounts != null && !bankAccounts.isEmpty) {
      val retBankAccounts = serializers.findValueSerializer(classOf[BankAccount], null)
      for (bankAccount <- bankAccounts) {
        retBankAccounts.serialize(bankAccount, gen, serializers)
      }
    }
    gen.writeEndArray()

    gen.writeFieldName("email")
    gen.writeStartArray()
    if (seller.email != null) {
      for (e <- seller.email)
        gen.writeString(e)
    }
    gen.writeEndArray()

    gen.writeFieldName("phone")
    gen.writeStartArray()
    val phone = seller.phone
    if (phone != null && !phone.isEmpty) {
      val retPhone = serializers.findValueSerializer(classOf[PhoneNumber], null)
      for (p <- phone) {
        retPhone.serialize(p, gen, serializers)
      }
    }
    gen.writeEndArray()

    gen.writeStringField("address", Option(seller.address) getOrElse "")

    gen.writeNumberField("favorCnt", Option(seller.favorCnt) getOrElse 0)
    gen.writeNumberField("rating", Option(seller.rating) getOrElse 0.0d)

    gen.writeEndObject()
  }
}