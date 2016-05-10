package core.formatter.geo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.geo.Locality
import com.lvxingpai.model.misc.{ IconText, ImageItem }

import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/11/19.
 */
class LocalitySerializer extends JsonSerializer[Locality] {

  override def serialize(geo: Locality, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", if (geo.id != null) geo.id.toString else "")
    gen.writeStringField("zhName", Option(geo.zhName) getOrElse "")
    gen.writeStringField("enName", Option(geo.enName) getOrElse "")
    gen.writeStringField("desc", Option(geo.desc) getOrElse "")
    gen.writeStringField("travelMonth", Option(geo.travelMonth) getOrElse "")

    // images
    gen.writeFieldName("images")
    gen.writeStartArray()
    val images = Option(geo.images) map (_.toSeq) getOrElse Seq()
    if (images.nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- images)
        ret.serialize(image, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeFieldName("remarks")
    gen.writeStartArray()
    val remarks = Option(geo.remarks) map (_.toSeq) getOrElse Seq()
    if (remarks.nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[IconText], null)
      for (r <- remarks)
        ret.serialize(r, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeNumberField("imageCnt", images.size)
    gen.writeStringField("playGuide", "http://h5.taozilvxing.com/city/items.php?tid=" + geo.id)
    gen.writeStringField("trafficInfoUrl", "http://h5.taozilvxing.com/city/traff-list.php?tid=" + geo.id)
    gen.writeNumberField("commoditiesCnt", if (geo.commodities != null) geo.commodities.size else 0)

    gen.writeEndObject()
  }
}
