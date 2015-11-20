package core.formatter.misc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.geo.Country
import core.formatter.BaseFormatter
import core.formatter.geo.SimpleCountrySerializer
import com.lvxingpai.model.misc.IdProof
import com.lvxingpai.model.marketplace.order.Person

/**
 * Created by pengyt on 2015/11/19.
 */
class PersonFormatter extends BaseFormatter {
  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    mapper.registerModule(DefaultScalaModule)
    module.addSerializer(classOf[Person], new PersonSerializer)
    module.addSerializer(classOf[IdProof], new IdProofSerializer)
    module.addSerializer(classOf[Country], new SimpleCountrySerializer)
    mapper.registerModule(module)
    mapper
  }
}
