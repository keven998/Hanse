package core.formatter.marketplace.seller

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.geo.GeoEntity
import com.lvxingpai.model.marketplace.seller.{ BankAccount, Seller }
import com.lvxingpai.model.misc.PhoneNumber
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/11/3.
 */
class SellerSerializer extends JsonSerializer[Seller] {

  override def serialize(seller: Seller, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeNumberField("id", seller.id)

    gen.writeFieldName("lang")
    gen.writeStartArray()
    if (seller.lang != null) {
      for (l: String <- seller.lang)
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

    gen.writeStringField("shopTitle", seller.shopTitle)

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

    if (seller.address != null)
      gen.writeStringField("address", seller.address)

    gen.writeNumberField("favorCnt", seller.favorCnt)

    gen.writeEndObject()
  }
}