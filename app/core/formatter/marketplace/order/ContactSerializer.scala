package core.formatter.marketplace.order

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.account.RealNameInfo
import com.lvxingpai.model.misc.PhoneNumber

/**
 * Created by topy on 2015/11/17.
 */
class ContactAndTravellersSerializer extends JsonSerializer[RealNameInfo] {

  override def serialize(person: RealNameInfo, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    // contact
    gen.writeStringField("surname", Option(person.surname) getOrElse "")
    gen.writeStringField("givenName", Option(person.givenName) getOrElse "")

    gen.writeFieldName("tel")
    val tel = person.tel
    if (tel != null) {
      val retTel = serializers.findValueSerializer(classOf[PhoneNumber], null)
      retTel.serialize(tel, gen, serializers)
    }
    gen.writeStringField("email", Option(person.email) getOrElse "")

    gen.writeEndObject()
  }
}
