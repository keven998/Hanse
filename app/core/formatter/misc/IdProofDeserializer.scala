package core.formatter.misc

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.{ DeserializationContext, JsonDeserializer, JsonNode, ObjectMapper }
import com.lvxingpai.model.account.{ ChineseID, IdProof, Passport }

/**
 * Created by pengyt on 2015/11/19.
 */
class IdProofDeserializer extends JsonDeserializer[IdProof] {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): IdProof = {
    val node = (new ObjectMapper).readTree[JsonNode](p)

    val idType = node.get("idType").asText()
    idType match {
      case "chineseID" =>
        val number = node.get("number").asText()
        val chineseID = new ChineseID()
        chineseID.number = number
        chineseID
      case "passport" =>
        val number = node.get("number").asText()
        val nation = node.get("nation").asText()
        val passport = new Passport()
        passport.number = number
        passport.nation = nation
        passport
    }
  }
}
