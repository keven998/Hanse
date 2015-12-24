package core.formatter.misc

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper, DeserializationContext, JsonDeserializer }
import com.lvxingpai.model.misc.PhoneNumber

/**
 * Created by pengyt on 2015/11/19.
 */
class PhoneNumberDerializer extends JsonDeserializer[PhoneNumber] {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): PhoneNumber = {
    val node = (new ObjectMapper).readTree[JsonNode](p)
    val dialCode = if (node.has("dialCode")) node.get("dialCode").asInt() else 0
    val number = if (node.has("number")) node.get("number").asLong() else 0
    val phoneNumber = new PhoneNumber
    phoneNumber.dialCode = dialCode
    phoneNumber.number = number
    phoneNumber
  }

}
