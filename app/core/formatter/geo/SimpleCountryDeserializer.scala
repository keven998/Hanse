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

    val id = if (node.has("id")) new ObjectId(node.get("id").asText()) else null
    val zhName = if (node.has("zhName")) node.get("zhName").asText() else null

    val country = new Country
    country.id = id
    country.zhName = zhName
    country
  }
}
