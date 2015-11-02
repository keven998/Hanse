package core.formatter.marketplace

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.lvxingpai.model.marketplace.Commodity
import core.formatter.BaseFormatter

/**
 * Created by pengyt on 2015/11/2.
 */
class CommodityFormatter extends BaseFormatter {

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[Commodity], new CommoditySerializer)
    mapper.registerModule(module)
    mapper
  }
}
