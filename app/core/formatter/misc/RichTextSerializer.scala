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

    gen.writeStringField("title", richText.title)
    gen.writeStringField("summary", richText.summary)
    gen.writeStringField("body", richText.body)

    gen.writeEndObject()
  }
}
