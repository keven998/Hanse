package core.formatter.misc

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.account._

/**
 * Created by pengyt on 2015/11/19.
 */
class IdProofSerializer extends JsonSerializer[IdProof] {

  override def serialize(idProof: IdProof, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    idProof match {
      case chineseID: ChineseID =>
        gen.writeStringField("number", Option(chineseID.number) getOrElse "")
      case t: TWPermit =>
        gen.writeStringField("number", Option(t.number) getOrElse "")
      case h: HMPermit =>
        gen.writeStringField("number", Option(h.number) getOrElse "")
      case passport: Passport =>
        gen.writeStringField("number", Option(passport.number) getOrElse "")
        gen.writeStringField("nation", Option(passport.nation) getOrElse "")
    }
    gen.writeStringField("idType", Option(idProof.idType) getOrElse "")
    gen.writeEndObject()
  }
}
