package core.formatter.misc

import java.util
import java.util.Date

import com.fasterxml.jackson.core.{ JsonFactory, JsonParser }
import com.fasterxml.jackson.databind.`type`.TypeFactory
import com.fasterxml.jackson.databind.node.NullNode
import com.fasterxml.jackson.databind.{ DeserializationContext, JsonDeserializer, JsonNode }
import com.lvxingpai.model.account.{ Gender, IdProof, RealNameInfo }
import com.lvxingpai.model.misc.PhoneNumber
import org.joda.time.format.DateTimeFormat

/**
 * Created by pengyt on 2015/11/19.
 */
class PersonDeserializer extends JsonDeserializer[RealNameInfo] {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): RealNameInfo = {
    val node = p.getCodec.readTree[JsonNode](p)
    val surname = node.get("surname").asText()
    val givenName = node.get("givenName").asText()
    val gender = node.get("gender") match {
      case _: NullNode => null
      case item => if (item.asText().equals(Gender.Male.toString)) Gender.Male.toString else Gender.Female.toString
    }
    val birthday = node.get("birthday").asText()
    val dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
    val birthdayDate = try {
      val millis = dateTimeFormatter.parseMillis(birthday)
      new Date(millis)
    } catch {
      case e: IllegalArgumentException => null
    }

    val javaTypeIdProof = TypeFactory.defaultInstance().constructType(classOf[IdProof])
    val deserializerIdProof = ctxt.findRootValueDeserializer(javaTypeIdProof)
    val jsonFactory = new JsonFactory
    val parserIdProof = jsonFactory.createParser(node.get("idProof").toString)
    val idProof = deserializerIdProof.deserialize(parserIdProof, ctxt).asInstanceOf[IdProof]

    val tel = node.get("tel") match {
      case _: NullNode => null
      case item: JsonNode =>
        val javaTypePhoneNumber = TypeFactory.defaultInstance().constructType(classOf[PhoneNumber])
        val deserializerPhoneNumber = ctxt.findRootValueDeserializer(javaTypePhoneNumber)
        val parserPhoneNumber = jsonFactory.createParser(item.toString)
        deserializerPhoneNumber.deserialize(parserPhoneNumber, ctxt).asInstanceOf[PhoneNumber]
    }

    val email = node.get("email") match {
      case _: NullNode => null
      case item: JsonNode => item.asText()
    }

    val person = new RealNameInfo()

    person.surname = surname
    person.givenName = givenName
    person.gender = gender
    person.birthday = birthdayDate
    person.identities = util.Arrays.asList(idProof)
    person.tel = tel
    person.email = email
    person
  }
}