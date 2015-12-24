package core.formatter.misc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.lvxingpai.model.misc.PhoneNumber

/**
 * Created by pengyt on 2015/11/19.
 */
object PhoneNumberParser {
  def apply(contents: String) = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addDeserializer(classOf[PhoneNumber], new PhoneNumberDerializer())
    mapper.registerModule(module)
    val result = mapper.readValue(contents, classOf[PhoneNumber])
    result
  }
}
