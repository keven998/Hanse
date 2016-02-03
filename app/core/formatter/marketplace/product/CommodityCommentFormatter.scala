package core.formatter.marketplace.product

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{ JsonSerializer, ObjectMapper, SerializerProvider }
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.account.UserInfo
import com.lvxingpai.model.marketplace.order.Order
import com.lvxingpai.model.marketplace.product._
import com.lvxingpai.model.misc.ImageItem
import core.formatter.BaseFormatter
import core.formatter.misc.ImageItemSerializer
import core.formatter.user.UserSerializer

import scala.collection.JavaConversions._
import scala.language.postfixOps

/**
 * Created by topy on 2016/1/25.
 */

class CommodityCommentFormatter extends BaseFormatter {

  override protected val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    mapper.registerModule(DefaultScalaModule)
    module.addSerializer(classOf[CommodityComment], new CommodityCommentSerializer)
    module.addSerializer(classOf[BaseCommodityComment], new CommodityReplySerializer)
    module.addSerializer(classOf[UserInfo], new UserSerializer)
    module.addSerializer(classOf[Order], new CommentOrderSerializer)
    module.addSerializer(classOf[Commodity], new CommentCommoditySerializer)
    module.addSerializer(classOf[CommodityPlan], new CommentCommodityPlanSerializer)

    module.addSerializer(classOf[ImageItem], new ImageItemSerializer)
    mapper.registerModule(module)
    mapper
  }

  class CommodityCommentSerializer extends JsonSerializer[CommodityComment] {

    override def serialize(c: CommodityComment, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
      gen.writeStartObject()
      gen.writeStringField("id", Option(c.id.toString) getOrElse "")
      gen.writeStringField("contents", Option(c.contents) getOrElse "")
      gen.writeNumberField("rating", c.rating)
      gen.writeBooleanField("anonymous", c.anonymous)

      gen.writeFieldName("user")
      val userInfo = c.user
      if (Option(userInfo).nonEmpty && !c.anonymous)
        serializers.findValueSerializer(classOf[UserInfo], null).serialize(userInfo, gen, serializers)
      else {
        gen.writeStartObject()
        gen.writeEndObject()
      }

      // images
      gen.writeFieldName("images")
      gen.writeStartArray()
      val images = c.images
      if (images != null) {
        val ret = serializers.findValueSerializer(classOf[ImageItem], null)
        for (image <- images)
          ret.serialize(image, gen, serializers)
      }
      gen.writeEndArray()

      gen.writeFieldName("reply")
      val reply = c.reply
      gen.writeStartObject()
      if (Option(reply).nonEmpty)
        serializers.findValueSerializer(classOf[BaseCommodityComment], null).serialize(reply, gen, serializers)
      gen.writeEndObject()

      gen.writeFieldName("order")
      val order = c.order
      if (Option(order).nonEmpty)
        serializers.findValueSerializer(classOf[Order], null).serialize(order, gen, serializers)
      else {
        gen.writeStartObject()
        gen.writeEndObject()
      }

      gen.writeNumberField("createTime", if (c.createTime != null) c.createTime.getTime else 0)
      gen.writeNumberField("updateTime", if (c.updateTime != null) c.updateTime.getTime else 0)
      gen.writeEndObject()
    }
  }

  class CommodityReplySerializer extends JsonSerializer[BaseCommodityComment] {

    override def serialize(b: BaseCommodityComment, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
      gen.writeStringField("contents", Option(b.contents) getOrElse "")

      // images
      gen.writeFieldName("images")
      gen.writeStartArray()
      val images = b.images
      if (images != null) {
        val ret = serializers.findValueSerializer(classOf[ImageItem], null)
        for (image <- images)
          ret.serialize(image, gen, serializers)
      }
      gen.writeEndArray()
    }
  }

  class CommentOrderSerializer extends JsonSerializer[Order] {

    override def serialize(order: Order, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
      gen.writeStartObject()
      //gen.writeStringField("id", Option(order.id.toString) getOrElse "")
      gen.writeNumberField("orderId", order.orderId)
      gen.writeStringField("status", Option(order.status) getOrElse "")
      // Commodity
      gen.writeFieldName("commodity")
      val commodity = order.commodity
      if (commodity != null) {
        val retSeller = serializers.findValueSerializer(classOf[Commodity], null)
        retSeller.serialize(commodity, gen, serializers)
      }
      gen.writeEndObject()
    }
  }

  class CommentCommoditySerializer extends JsonSerializer[Commodity] {

    override def serialize(commodity: Commodity, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
      gen.writeStartObject()
      if (commodity.id != null)
        gen.writeStringField("id", commodity.id.toString)
      gen.writeNumberField("commodityId", commodity.commodityId)
      gen.writeStringField("title", Option(commodity.title) getOrElse "")

      gen.writeFieldName("plans")
      gen.writeStartArray()
      val plans = commodity.plans
      if (plans != null) {
        val ret = serializers.findValueSerializer(classOf[CommodityPlan], null)
        for (pl <- plans)
          ret.serialize(pl, gen, serializers)
      }
      gen.writeEndArray()

      gen.writeEndObject()
    }
  }

  class CommentCommodityPlanSerializer extends JsonSerializer[CommodityPlan] {

    override def serialize(commodityPlan: CommodityPlan, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
      gen.writeStartObject()
      if (commodityPlan.planId != null)
        gen.writeStringField("planId", commodityPlan.planId)
      gen.writeStringField("title", Option(commodityPlan.title) getOrElse "")

      gen.writeEndArray()
      gen.writeEndObject()
    }
  }

}

object CommodityCommentFormatter {

  lazy val instance = new CommodityCommentFormatter
}
