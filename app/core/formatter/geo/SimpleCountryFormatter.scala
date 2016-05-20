package core.formatter.geo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.geo.Country
import core.formatter.BaseFormatter

/**
 * Created by pengyt on 2015/11/13.
 */
class SimpleCountryFormatter() extends BaseFormatter {
  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    mapper.registerModule(DefaultScalaModule)
    module.addSerializer(classOf[Country], new SimpleCountrySerializer)
    mapper.registerModule(module)
    mapper
  }
}
object SimpleCountryFormatter {
  lazy val instance = new SimpleCountryFormatter
}