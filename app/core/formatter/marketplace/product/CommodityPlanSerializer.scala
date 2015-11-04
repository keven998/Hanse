package core.formatter.marketplace.product

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{SerializerProvider, JsonSerializer}
import com.lvxingpai.model.marketplace.product.{StockInfo, Pricing, CommodityPlan}
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/11/4.
 */
class CommodityPlanSerializer extends JsonSerializer[CommodityPlan] {

  override def serialize(commodityPlan: CommodityPlan, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("planId", commodityPlan.planId)
    if(commodityPlan.title != null)
      gen.writeStringField("title", commodityPlan.title)
    if(commodityPlan.desc != null)
      gen.writeStringField("desc", commodityPlan.desc)

    gen.writeFieldName("pricing")
    gen.writeStartArray()
    val pricing = commodityPlan.pricing
    if (pricing != null && !pricing.isEmpty) {
      val retPricing = serializers.findValueSerializer(classOf[Pricing], null)
      for (p <- pricing) {
        retPricing.serialize(p, gen, serializers)
      }
    }
    gen.writeEndArray()

    gen.writeNumberField("marketPrice", commodityPlan.marketPrice)
    gen.writeNumberField("price", commodityPlan.price)

    gen.writeFieldName("stockInfo")
    gen.writeStartArray()
    val stockInfo = commodityPlan.stockInfo
    if (stockInfo != null && !stockInfo.isEmpty) {
      val retStockInfo = serializers.findValueSerializer(classOf[StockInfo], null)
      for (s <- stockInfo) {
        retStockInfo.serialize(s, gen, serializers)
      }
    }
    gen.writeEndArray()

    gen.writeFieldName("timeRange")
    gen.writeStartArray()
    if(commodityPlan.timeRange != null) {
      for(t <- commodityPlan.timeRange)
        gen.writeNumber(t.getTime)
    }
    gen.writeEndArray()

    gen.writeEndObject()
  }
}
