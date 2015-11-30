package core.formatter.marketplace.order

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{ JsonSerializer, ObjectMapper, SerializerProvider }
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.marketplace.order.Order
import core.formatter.BaseFormatter

/**
 * Created by pengyt on 2015/11/21.
 */
class OrderStatusFormatter extends BaseFormatter {

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    mapper.registerModule(DefaultScalaModule)
    module.addSerializer(classOf[Order], new OrderStatusSerializer)
    mapper.registerModule(module)
    mapper
  }

  class OrderStatusSerializer extends JsonSerializer[Order] {

    override def serialize(order: Order, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
      gen.writeStartObject()
      gen.writeStringField("status", Option(order.status) getOrElse "")
      gen.writeEndObject()
    }
  }

}
