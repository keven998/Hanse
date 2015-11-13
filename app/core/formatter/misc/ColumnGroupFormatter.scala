package core.formatter.misc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import core.formatter.BaseFormatter
import core.model.misc.ColumnGroup

/**
 * Created by pengyt on 2015/11/13.
 */
class ColumnGroupFormatter  extends BaseFormatter {

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[ColumnGroup], new ColumnGroupSerializer)
    mapper.registerModule(module)
    mapper
  }
}

