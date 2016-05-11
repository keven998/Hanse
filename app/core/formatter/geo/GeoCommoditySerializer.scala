package core.formatter.geo

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.marketplace.seller.Seller
import core.model.misc.GeoCommodity

import scala.collection.JavaConversions._
/**
 * Created by pengyt on 2015/11/19.
 */
class GeoCommoditySerializer extends JsonSerializer[GeoCommodity] {

  override def serialize(geo: GeoCommodity, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("geoId", Option(geo.geoId.toString) getOrElse "")

    gen.writeFieldName("sellers")
    gen.writeStartArray()
    val orderAct = serializers.findValueSerializer(classOf[Seller], null)
    Option(geo.sellers) map (_.toSeq) getOrElse Seq() foreach (orderAct.serialize(_, gen, serializers))
    gen.writeEndArray()

    gen.writeEndObject()
  }
}
