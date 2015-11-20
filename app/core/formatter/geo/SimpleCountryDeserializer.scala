package core.formatter.geo

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper, DeserializationContext, JsonDeserializer }
import com.lvxingpai.model.geo.Country
import org.bson.types.ObjectId

/**
 * Created by pengyt on 2015/11/19.
 */
class SimpleCountryDeserializer extends JsonDeserializer[Country] {
  override def deserialize(p: JsonParser, ctxt: DeserializationContext): Country = {
    val node = (new ObjectMapper).readTree[JsonNode](p)

    val id = node.get("id").asText()
    val zhName = node.get("zhName").asText()

    val country = new Country
    country.id = new ObjectId(id)
    country.zhName = zhName
    country
  }
}
