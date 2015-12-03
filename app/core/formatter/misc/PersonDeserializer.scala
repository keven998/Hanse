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
    val surname = if (node.has("surname")) node.get("surname").asText() else ""
    val givenName = if (node.has("givenName")) node.get("givenName").asText() else ""
    val gender = if (node.has("gender")) {
      node.get("gender") match {
        case _: NullNode => null
        case item => if (item.asText().equals(Gender.Male.toString)) Gender.Male.toString else Gender.Female.toString
      }
    } else null

    val birthdayDate = if (node.has("birthday")) {
      val birthday = node.get("birthday").asText()
      val dateTimeFormatter = DateTimeFormat.forPattern("yyyy-MM-dd")
      try {
        val millis = dateTimeFormatter.parseMillis(birthday)
        new Date(millis)
      } catch {
        case e: IllegalArgumentException => null
      }
    } else null

    val jsonFactory = new JsonFactory

    val idProof = if (node.has("idProof")) {
      val javaTypeIdProof = TypeFactory.defaultInstance().constructType(classOf[IdProof])
      val deserializerIdProof = ctxt.findRootValueDeserializer(javaTypeIdProof)
      val parserIdProof = jsonFactory.createParser(node.get("idProof").toString)
      val ret = deserializerIdProof.deserialize(parserIdProof, ctxt).asInstanceOf[IdProof]
      util.Arrays.asList(ret)
    } else null

    val tel = if (node.has("tel"))
      node.get("tel") match {
        case _: NullNode => null
        case item: JsonNode =>
          val javaTypePhoneNumber = TypeFactory.defaultInstance().constructType(classOf[PhoneNumber])
          val deserializerPhoneNumber = ctxt.findRootValueDeserializer(javaTypePhoneNumber)
          val parserPhoneNumber = jsonFactory.createParser(item.toString)
          deserializerPhoneNumber.deserialize(parserPhoneNumber, ctxt).asInstanceOf[PhoneNumber]
      }
    else null

    val email = if (node.has("email")) {
      node.get("email").asText()
    } else null

    val person = new RealNameInfo()

    person.surname = surname
    person.givenName = givenName
    person.gender = gender
    person.birthday = birthdayDate
    person.identities = idProof
    person.tel = tel
    person.email = email
    person
  }
}