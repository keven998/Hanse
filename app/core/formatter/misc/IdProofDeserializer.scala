package core.formatter.misc

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.{ DeserializationContext, JsonDeserializer, JsonNode, ObjectMapper }
import com.lvxingpai.model.account._

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
        chineseID.idType = idType
        chineseID
      case "passport" =>
        val number = node.get("number").asText()
        val nation = if (node.has("nation")) node.get("nation").asText() else ""
        val passport = new Passport()
        passport.number = number
        passport.nation = nation
        passport.idType = idType
        passport
      case "TWPermit" =>
        val number = node.get("number").asText()
        val passport = new TWPermit()
        passport.number = number
        passport.idType = idType
        passport
      case "HMPermit" =>
        val number = node.get("number").asText()
        val passport = new HMPermit()
        passport.number = number
        passport.idType = idType
        passport
    }
  }
}
