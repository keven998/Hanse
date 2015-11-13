package core.formatter.marketplace.product

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.marketplace.product.{ CommodityPlan, Commodity }
import com.lvxingpai.model.marketplace.seller.Seller
import scala.collection.JavaConversions._

/**
 * Created by pengyt on 2015/11/3.
 */
class CommoditySerializer extends JsonSerializer[Commodity] {

  override def serialize(commodity: Commodity, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", commodity.id.toString)
    gen.writeNumberField("commodityId", commodity.commodityId)

    // 商家
    gen.writeFieldName("seller")
    val seller = commodity.seller
    if (seller != null) {
      val retSeller = serializers.findValueSerializer(classOf[Seller], null)
      retSeller.serialize(seller, gen, serializers)
    }

    gen.writeStringField("title", commodity.title)

    //    if (commodity.desc != null)
    //      gen.writeStringField("desc", commodity.desc)

    // 商品套餐
    gen.writeFieldName("plans")
    gen.writeStartArray()
    val plans = commodity.plans
    if (plans != null && !plans.isEmpty) {
      val retPlans = serializers.findValueSerializer(classOf[CommodityPlan], null)
      for (plan <- plans) {
        retPlans.serialize(plan, gen, serializers)
      }
    }
    gen.writeEndArray()

    gen.writeEndObject()
  }
}