package core.formatter.geo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.misc.{ ImageItem, IconText }

import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/11/4.
 */
class IconTextSerializer extends JsonSerializer[IconText] {

  override def serialize(it: IconText, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    gen.writeStringField("title", Option(it.title) getOrElse "")
    gen.writeStringField("url", Option(it.url) getOrElse "")

    gen.writeFieldName("cover")
    val cover = it.cover
    val retCover = if (cover != null) serializers.findValueSerializer(classOf[ImageItem], null)
    else serializers.findNullValueSerializer(null)
    retCover.serialize(cover, gen, serializers)

    // images
    gen.writeFieldName("images")
    gen.writeStartArray()
    val images = Option(it.images) map (_.toSeq) getOrElse Seq()
    if (images.nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- images)
        ret.serialize(image, gen, serializers)
    }
    gen.writeEndArray()
    gen.writeEndObject()
  }
}