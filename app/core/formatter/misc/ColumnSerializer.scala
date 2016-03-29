package core.formatter.misc

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import com.lvxingpai.model.misc.ImageItem
import core.model.misc.Column
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/11/13.
 */
class ColumnSerializer(userId: Option[Long]) extends JsonSerializer[Column] {

  override def serialize(column: Column, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    gen.writeStringField("title", Option(column.title) getOrElse "")
    // images
    gen.writeFieldName("images")
    gen.writeStartArray()
    val images = column.images
    if (images.nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- images)
        ret.serialize(image, gen, serializers)
    }
    gen.writeEndArray()

    val link = column.link
    val linkUrl: Option[String] = userId map (u => Option(link) match {
      case None => ""
      case urls if urls.get.contains('?') => s"$link&userId=$u"
      case url if !url.get.contains('?') => s"$link?userId=$u"
      case _ => ""
    }) orElse Some(link)
    gen.writeStringField("link", linkUrl.get)

    gen.writeEndObject()
  }
}