package core.formatter.misc

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import core.model.misc.LocalityArticle

/**
 * Created by pengyt on 2015/11/13.
 */
class LocalityArticleSerializer() extends JsonSerializer[LocalityArticle] {

  override def serialize(art: LocalityArticle, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    gen.writeNumberField("articleId", art.articleId)
    gen.writeStringField("title", Option(art.title) getOrElse "")

    gen.writeStringField("desc", Option(art.desc) getOrElse "")

    // TODO 链接
    gen.writeStringField("url", "http://h5.lvxingpai.com/cityplay/cityplay_detail.php?tid=" + art.articleId)

    //    gen.writeFieldName("country")
    //    val country = art.country
    //    if (country != null) {
    //      val retSeller = serializers.findValueSerializer(classOf[Country], null)
    //      retSeller.serialize(country, gen, serializers)
    //    }
    //
    //    gen.writeFieldName("locality")
    //    val loc = art.locality
    //    if (loc != null) {
    //      val retSeller = serializers.findValueSerializer(classOf[Locality], null)
    //      retSeller.serialize(loc, gen, serializers)
    //    }
    gen.writeStringField("status", Option(art.status) getOrElse "")
    gen.writeNumberField("createTime", if (art.createTime != null) art.createTime.getTime else 0)
    gen.writeNumberField("updateTime", if (art.updateTime != null) art.updateTime.getTime else 0)

    gen.writeEndObject()
  }
}