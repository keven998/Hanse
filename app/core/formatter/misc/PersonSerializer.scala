package core.formatter.misc

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.account.Gender
import com.lvxingpai.model.misc.PhoneNumber
import core.model.trade.order.Person

/**
 * Created by pengyt on 2015/11/17.
 */
class PersonSerializer extends JsonSerializer[Person] {

  override def serialize(person: Person, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    gen.writeStringField("surname", person.surname)
    gen.writeStringField("givenName", person.givenName)
    gen.writeStringField("gender", if (person.gender == Gender.Male) "male" else "female")
    gen.writeStringField("birthday", person.birthday.toString)
    gen.writeStringField("fullName", person.fullName)

    // TODO 如何序列化idProof?
    //    var idProof: IdProof = _
    val idProof = person.idProof

    gen.writeFieldName("tel")
    val tel = person.tel
    if (tel != null) {
      val retTel = serializers.findValueSerializer(classOf[PhoneNumber], null)
      retTel.serialize(tel, gen, serializers)
    }
    gen.writeStringField("email", person.email)
    gen.writeEndObject()
  }
}
