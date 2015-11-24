package core.formatter.misc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.account.{ IdProof, RealNameInfo }
import com.lvxingpai.model.geo.Country
import core.formatter.BaseFormatter
import core.formatter.geo.SimpleCountrySerializer

/**
 * Created by pengyt on 2015/11/19.
 */
class PersonFormatter extends BaseFormatter {
  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    mapper.registerModule(DefaultScalaModule)
    module.addSerializer(classOf[RealNameInfo], new RealNameInfoSerializer)
    module.addSerializer(classOf[IdProof], new IdProofSerializer)
    module.addSerializer(classOf[Country], new SimpleCountrySerializer)
    mapper.registerModule(module)
    mapper
  }
}
