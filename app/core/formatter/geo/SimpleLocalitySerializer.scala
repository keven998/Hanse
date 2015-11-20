package core.formatter.geo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.geo.GeoEntity

/**
 * Created by pengyt on 2015/11/19.
 */
class SimpleLocalitySerializer extends JsonSerializer[GeoEntity] {

  override def serialize(geo: GeoEntity, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", geo.id.toString)
    gen.writeStringField("zhName", geo.zhName)
    gen.writeStringField("enName", geo.enName)

    gen.writeEndObject()
  }
}
