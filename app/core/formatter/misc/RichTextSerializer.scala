package core.formatter.misc

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import com.lvxingpai.model.misc.RichText

/**
 * Created by pengyt on 2015/11/13.
 */
class RichTextSerializer extends JsonSerializer[RichText] {

  override def serialize(richText: RichText, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("title", Option(richText.title) getOrElse "")
    gen.writeStringField("summary", Option(richText.summary) getOrElse "")
    gen.writeStringField("body", Option(richText.body) getOrElse "")

    gen.writeEndObject()
  }
}
