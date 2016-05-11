package core.formatter.geo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.geo.Country
import com.lvxingpai.model.misc.{ IconText, ImageItem }

import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/11/19.
 */
class CountrySerializer extends JsonSerializer[Country] {

  override def serialize(country: Country, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", if (country.id != null) country.id.toString else "")
    gen.writeStringField("zhName", Option(country.zhName) getOrElse "")
    gen.writeStringField("enName", Option(country.enName) getOrElse "")
    gen.writeStringField("desc", Option(country.desc) getOrElse "")

    gen.writeFieldName("cover")
    val cover = country.cover
    val retCover = if (cover != null) serializers.findValueSerializer(classOf[ImageItem], null)
    else serializers.findNullValueSerializer(null)
    retCover.serialize(cover, gen, serializers)

    // images
    gen.writeFieldName("images")
    gen.writeStartArray()
    val images = Option(country.images) map (_.toSeq) getOrElse Seq()
    if (images.nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
      for (image <- images)
        ret.serialize(image, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeNumberField("imageCnt", images.size)

    gen.writeFieldName("remarks")
    gen.writeStartArray()
    val remarks = Option(country.remarks) map (_.toSeq) getOrElse Seq()
    if (remarks.nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[IconText], null)
      for (r <- remarks)
        ret.serialize(r, gen, serializers)
    }
    gen.writeEndArray()

    // TODO
    gen.writeStringField("playGuide", "")
    gen.writeStringField("trafficInfoUrl", "")
    gen.writeNumberField("commoditiesCnt", 56)

    gen.writeEndObject()
  }
}
