package core.formatter.misc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.lvxingpai.model.account.{ RealNameInfo, IdProof }
import com.lvxingpai.model.geo.Country
import com.lvxingpai.model.misc.PhoneNumber
import core.formatter.geo.SimpleCountryDeserializer

/**
 * Created by pengyt on 2015/11/19.
 */
object PersonParser {
  def apply(contents: String) = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addDeserializer(classOf[RealNameInfo], new PersonDeserializer())
    module.addDeserializer(classOf[IdProof], new IdProofDeserializer())
    module.addDeserializer(classOf[PhoneNumber], new PhoneNumberDerializer())
    module.addDeserializer(classOf[Country], new SimpleCountryDeserializer())
    mapper.registerModule(module)

    val result = mapper.readValue(contents, classOf[RealNameInfo])
    result
  }
}
