package core.formatter.misc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.misc.ImageItem
import core.formatter.BaseFormatter
import core.model.misc.Column

/**
 * Created by pengyt on 2015/11/13.
 */
class ColumnFormatter(userId: Option[Long]) extends BaseFormatter {
  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    mapper.registerModule(DefaultScalaModule)
    module.addSerializer(classOf[Column], new ColumnSerializer(userId))
    module.addSerializer(classOf[ImageItem], new ImageItemSerializer)
    mapper.registerModule(module)
    mapper
  }
}
