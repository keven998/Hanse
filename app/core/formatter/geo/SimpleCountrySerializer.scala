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

    gen.writeStringField("id", if (country.id != null) country.id.toString else "")
    gen.writeStringField("zhName", Option(country.zhName) getOrElse "")
    gen.writeStringField("enName", Option(country.enName) getOrElse "")

    gen.writeEndObject()
  }
}
