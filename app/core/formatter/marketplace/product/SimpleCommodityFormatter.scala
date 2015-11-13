package core.formatter.marketplace.product

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.lvxingpai.model.marketplace.product.Commodity
import core.formatter.BaseFormatter

/**
 * Created by pengyt on 2015/11/13.
 */
class SimpleCommodityFormatter extends BaseFormatter {

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[Commodity], new SimpleCommoditySerializer)
    mapper.registerModule(module)
    mapper
  }
}