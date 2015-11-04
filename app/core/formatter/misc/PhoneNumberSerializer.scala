package core.formatter.misc

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import com.lvxingpai.model.misc.PhoneNumber

/**
 * Created by pengyt on 2015/11/4.
 */
class PhoneNumberSerializer extends JsonSerializer[PhoneNumber] {

  override def serialize(phoneNumber: PhoneNumber, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeNumberField("dialCode", phoneNumber.dialCode)
    gen.writeNumberField("number", phoneNumber.number)

    gen.writeEndObject()
  }
}
