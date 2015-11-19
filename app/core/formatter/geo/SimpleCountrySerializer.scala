package core.formatter.geo
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.geo.Country
/**
 * Created by pengyt on 2015/11/19.
 */
class SimpleCountrySerializer extends JsonSerializer[Country] {

  override def serialize(country: Country, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", country.id.toString)

    gen.writeStringField("zhName", country.zhName)
    gen.writeStringField("enName", country.enName)

    gen.writeEndObject()
  }
}
