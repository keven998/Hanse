package core.formatter.misc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.geo.{ Country, Locality }
import core.formatter.BaseFormatter
import core.formatter.geo.{ SimpleCountrySerializer, SimpleLocalitySerializer }
import core.model.misc.LocalityArticle

/**
 * Created by pengyt on 2015/11/13.
 */
class LocalityArticleFormatter() extends BaseFormatter {
  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    mapper.registerModule(DefaultScalaModule)
    module.addSerializer(classOf[LocalityArticle], new LocalityArticleSerializer())
    module.addSerializer(classOf[Locality], new SimpleLocalitySerializer)
    module.addSerializer(classOf[Country], new SimpleCountrySerializer)
    mapper.registerModule(module)
    mapper
  }
}
