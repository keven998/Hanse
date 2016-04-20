package core.formatter.geo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.geo.Locality
import core.formatter.BaseFormatter

/**
 * Created by topy on 2016/2/19.
 */
class SimpleLocalityFormatter extends BaseFormatter {

  override protected val objectMapper = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    val module = new SimpleModule()
    module.addSerializer(classOf[Locality], new SimpleLocalitySerializer)
    mapper.registerModule(module)
    mapper
  }
}
object SimpleLocalityFormatter {
  lazy val instance = new SimpleLocalityFormatter
}

