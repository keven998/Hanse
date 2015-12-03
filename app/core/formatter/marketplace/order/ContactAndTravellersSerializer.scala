package core.formatter.marketplace.order

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.account.{ IdProof, RealNameInfo }
import com.lvxingpai.model.misc.PhoneNumber

import scala.collection.JavaConversions._

/**
 * Created by topy on 2015/11/17.
 */
class ContactAndTravellersSerializer extends JsonSerializer[RealNameInfo] {

  override def serialize(person: RealNameInfo, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    // contact
    gen.writeStringField("surname", Option(person.surname) getOrElse "")
    gen.writeStringField("givenName", Option(person.givenName) getOrElse "")
    gen.writeStringField("gender", Option(person.gender) getOrElse "")
    gen.writeStringField("email", Option(person.email) getOrElse "")
    gen.writeNumberField("birthday", if (person.birthday != null) person.birthday.getTime else 0)

    gen.writeFieldName("tel")
    val tel = person.tel
    if (tel != null) {
      val retTel = serializers.findValueSerializer(classOf[PhoneNumber], null)
      retTel.serialize(tel, gen, serializers)
    } else serializers.findNullValueSerializer(null).serialize(tel, gen, serializers)

    //
    gen.writeFieldName("identities")
    gen.writeStartArray()
    val idProof = person.identities
    if (idProof != null) {
      val retIdProof = serializers.findValueSerializer(classOf[IdProof], null)
      for (id <- idProof) {
        retIdProof.serialize(id, gen, serializers)
      }
    } else serializers.findNullValueSerializer(null)
    gen.writeEndArray()

    gen.writeEndObject()
  }
}
