package core.formatter.misc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.lvxingpai.model.misc.ImageItem
import core.formatter.BaseFormatter
import core.formatter.formatter.taozi.ImageItemSerializer
import core.model.misc.Column

/**
 * Created by pengyt on 2015/11/13.
 */
class ColumnFormatter extends BaseFormatter {
  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[Column], new ColumnSerializer)
    module.addSerializer(classOf[ImageItem], new ImageItemSerializer)
    mapper.registerModule(module)
    mapper
  }
}
