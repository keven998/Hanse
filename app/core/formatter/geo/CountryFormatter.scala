package core.formatter.geo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.geo.Country
import com.lvxingpai.model.misc.ImageItem
import core.formatter.BaseFormatter
import core.formatter.misc.ImageItemSerializer

/**
 * Created by topy on 2016/2/19.
 */
class CountryFormatter extends BaseFormatter {

  override protected val objectMapper = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    val module = new SimpleModule()
    module.addSerializer(classOf[Country], new CountrySerializer)
    module.addSerializer(classOf[ImageItem], new ImageItemSerializer)
    mapper.registerModule(module)
    mapper
  }
}

object CountryFormatter {
  lazy val instance = new CountryFormatter
}

