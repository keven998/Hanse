package core.formatter.misc

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import com.lvxingpai.model.geo.Country
import com.lvxingpai.model.misc.{ Passport, ChineseID, IdProof }

/**
 * Created by pengyt on 2015/11/19.
 */
class IdProofSerializer extends JsonSerializer[IdProof] {

  override def serialize(idProof: IdProof, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    idProof match {
      case chineseID: ChineseID => gen.writeStringField("number", chineseID.number)
      case passport: Passport =>
        gen.writeStringField("number", passport.number)
        gen.writeFieldName("nation")
        val nation = passport.nation
        if (nation != null) {
          val retNation = serializers.findValueSerializer(classOf[Country], null)
          retNation.serialize(nation, gen, serializers)
        }
    }

    gen.writeEndObject()
  }
}
