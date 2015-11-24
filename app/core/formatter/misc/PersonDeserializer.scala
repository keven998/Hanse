package core.formatter.misc

import java.util.Date

import com.fasterxml.jackson.core.{ JsonFactory, JsonParser }
import com.fasterxml.jackson.databind.`type`.TypeFactory
import com.fasterxml.jackson.databind.node.{ NullNode, TextNode }
import com.fasterxml.jackson.databind.{ DeserializationContext, JsonDeserializer, JsonNode }
import com.lvxingpai.model.account.{ RealNameInfo, IdProof, Gender }
import com.lvxingpai.model.misc.PhoneNumber
import org.joda.time.format.{ DateTimeFormat, DateTimeFormatter }

/**
 * Created by pengyt on 2015/11/19.
 */
class PersonDeserializer extends JsonDeserializer[RealNameInfo] {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): RealNameInfo = {
    val node = p.getCodec.readTree[JsonNode](p)
    val surname = node.get("surname").asText()
    val givenName = node.get("givenName").asText()
    val gender = node.get("gender").asText()
    val birthday = node.get("birthday").asText()
    val dateTimeFormatter: DateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    val millis = dateTimeFormatter.parseMillis(birthday)
    val birthdayDate = new Date(millis)

    val javaTypeIdProof = TypeFactory.defaultInstance().constructType(classOf[IdProof])
    val deserializerIdProof = ctxt.findRootValueDeserializer(javaTypeIdProof)
    val jsonFactory = new JsonFactory
    val parserIdProof = jsonFactory.createParser(node.get("idProof").toString)
    val idProof = deserializerIdProof.deserialize(parserIdProof, ctxt).asInstanceOf[IdProof]

    val tel = node.get("tel") match {
      case _: NullNode => None
      case item: TextNode =>
        val javaTypePhoneNumber = TypeFactory.defaultInstance().constructType(classOf[PhoneNumber])
        val deserializerPhoneNumber = ctxt.findRootValueDeserializer(javaTypePhoneNumber)
        val parserPhoneNumber = jsonFactory.createParser(item.asText())
        val phoneNumber = deserializerPhoneNumber.deserialize(parserPhoneNumber, ctxt).asInstanceOf[PhoneNumber]
        Some(phoneNumber)
    }

    val email = node.get("email") match {
      case _: NullNode => None
      case item: TextNode => Some(item.asText())
    }

    val person = new RealNameInfo()

    person.surname = surname
    person.givenName = givenName
    person.gender = if (gender == "male") Gender.Male.toString else Gender.Female.toString
    person.birthday = birthdayDate
    // person.idProof = idProof
    if (tel.nonEmpty) person.tel = tel.get
    if (email.nonEmpty) person.email = email.get
    person
  }
}