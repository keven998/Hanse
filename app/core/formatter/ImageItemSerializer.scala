package core.formatter

package formatter.taozi

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.misc.ImageItem

/**
 * Created by pengyt on 2015/11/13.
 */
class ImageItemSerializer extends JsonSerializer[ImageItem] {

  override def serialize(imageItem: ImageItem, gen: JsonGenerator, serializers: SerializerProvider): Unit = {

    gen.writeStartObject()
    //    gen.writeStringField("caption", if (imageItem.caption != null) imageItem.caption else "")

    gen.writeStringField("key", Option(imageItem.key) getOrElse "")
    //    gen.writeStringField("bucket", if(imageItem.bucket != null) imageItem.bucket else "")
    gen.writeNumberField("width", Option(imageItem.width) getOrElse 0)
    gen.writeNumberField("height", Option(imageItem.height) getOrElse 0)
    //    val // http://7sbm17.com1.z0.glb.clouddn.com/lvxingpai-cover-20151009-480-800.png?imageView/1/w/1440/h/2392/q/85/format/jpg/interlace/1
    //    gen.writeStringField("url", f"$fullUrl%s?imageView2/2/w/$maxWidth%d")

    //    f"$fullUrl%s?imageView2/2/w/$maxWidth%d"
    //    f"$fullUrl%s?imageMogr2/auto-orient/strip/gravity/NorthWest/crop/!${widthValue.toInt}%dx${heightValue.toInt}%da$left%da$top%d/thumbnail/$maxWidth"
    //    gen.writeStringField("url", f"$fullUrl%s?imageView2/2/w/$maxWidth%d")

    gen.writeEndObject()
  }
}