package core.formatter.marketplace.product

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{ JsonSerializer, ObjectMapper, SerializerProvider }
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import core.formatter.BaseFormatter

/**
 * Created by topy on 2015/11/13.
 */
class CommodityCategoryFormatter extends BaseFormatter {

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    mapper.registerModule(DefaultScalaModule)
    module.addSerializer(classOf[Seq[String]], new CommodityCategorySerializer)
    mapper.registerModule(module)
    mapper
  }

  class CommodityCategorySerializer extends JsonSerializer[Seq[String]] {

    override def serialize(c: Seq[String], gen: JsonGenerator, serializers: SerializerProvider): Unit = {
      gen.writeStartObject()
      gen.writeFieldName("category")
      gen.writeStartArray()
      if (c != null) {
        for (t <- c)
          gen.writeString(t)
      }
      gen.writeEndArray()

      gen.writeEndObject()
    }
  }

}