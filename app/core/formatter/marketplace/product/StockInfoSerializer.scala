package core.formatter.marketplace.product

import scala.collection.JavaConversions._
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import com.lvxingpai.model.marketplace.product.StockInfo

/**
 * Created by pengyt on 2015/11/4.
 */
class StockInfoSerializer extends JsonSerializer[StockInfo] {

  override def serialize(stockInfo: StockInfo, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("status", Option(stockInfo.status) getOrElse "")
    gen.writeNumberField("quantity", Option(stockInfo.quantity) getOrElse 0)

    gen.writeFieldName("timeRange")
    gen.writeStartArray()
    if (stockInfo.timeRange != null) {
      for (t <- stockInfo.timeRange)
        gen.writeNumber(t.getTime)
    }
    gen.writeEndArray()

    gen.writeEndObject()
  }
}