package core.formatter.marketplace.order

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.guide.Guide
import com.lvxingpai.model.misc.ImageItem

import scala.collection.JavaConversions._

/**
 * Created by topy on 2016/3/30.
 */
class GuideSerializer extends JsonSerializer[Guide] {

  override def serialize(g: Guide, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", g.id.toString)
    gen.writeStringField("summary", g.summary)
    gen.writeStringField("title", g.title)

    // images
    gen.writeFieldName("images")
    gen.writeStartArray()
    val images = Option(g.images) map (_.toSeq) getOrElse Seq()
    if (images.nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- images)
        ret.serialize(image, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeEndObject()
  }
}
