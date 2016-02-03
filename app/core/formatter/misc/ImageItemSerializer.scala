package core.formatter.misc

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.misc.ImageItem

/**
 * Created by pengyt on 2015/11/13.
 */
class ImageItemSerializer extends JsonSerializer[ImageItem] {

  override def serialize(imageItem: ImageItem, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    if (imageItem != null) {
      if (imageItem.url != null)
        gen.writeStringField("url", imageItem.url)
      else {
        val vKey = imageItem.key
        val vBucket = imageItem.bucket
        val fullUrl = if (vBucket != null) s"http://$vBucket.qiniudn.com/$vKey" else s"http://images.lvxingpai.com/$vKey"
        gen.writeStringField("url", fullUrl)
      }
    }
    gen.writeEndObject()
  }

}