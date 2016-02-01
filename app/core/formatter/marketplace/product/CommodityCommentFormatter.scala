package core.formatter.marketplace.product

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.{ JsonSerializer, ObjectMapper, SerializerProvider }
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.account.UserInfo
import com.lvxingpai.model.marketplace.product.{ BaseCommodityComment, CommodityComment }
import com.lvxingpai.model.misc.ImageItem
import core.formatter.BaseFormatter
import core.formatter.misc.ImageItemSerializer
import core.formatter.user.UserSerializer

import scala.collection.JavaConversions._

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

}

object CommodityCommentFormatter {

  lazy val instance = new CommodityCommentFormatter
}
