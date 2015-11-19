package core.formatter.misc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.lvxingpai.model.geo.Country
import core.formatter.BaseFormatter
import core.formatter.geo.SimpleCountrySerializer
import core.model.trade.order.Person
import com.lvxingpai.model.misc.IdProof

/**
 * Created by pengyt on 2015/11/19.
 */
class PersonFormatter extends BaseFormatter {
  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[Person], new PersonSerializer)
    module.addSerializer(classOf[IdProof], new IdProofSerializer)
    module.addSerializer(classOf[Country], new SimpleCountrySerializer)
    mapper.registerModule(module)
    mapper
  }
}
