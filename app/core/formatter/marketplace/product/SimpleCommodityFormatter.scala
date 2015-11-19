package core.formatter.marketplace.product

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.lvxingpai.model.marketplace.product.Commodity
import com.lvxingpai.model.misc.ImageItem
import core.formatter.BaseFormatter
import core.formatter.formatter.taozi.ImageItemSerializer

/**
 * Created by pengyt on 2015/11/13.
 */
class SimpleCommodityFormatter extends BaseFormatter {

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[Commodity], new SimpleCommoditySerializer)
    module.addSerializer(classOf[ImageItem], new ImageItemSerializer)
    mapper.registerModule(module)
    mapper
  }
}