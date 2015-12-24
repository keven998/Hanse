package core.formatter.marketplace.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.account.{ IdProof, RealNameInfo }
import com.lvxingpai.model.misc.PhoneNumber
import core.formatter.BaseFormatter
import core.formatter.misc._

/**
 * Created by pengyt on 2015/11/21.
 */
class TravellersFormatter extends BaseFormatter {

  override protected val objectMapper = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)

    val module = new SimpleModule()
    module.addSerializer(classOf[RealNameInfo], new RealNameInfoSerializer)
    module.addSerializer(classOf[IdProof], new IdProofSerializer)
    module.addSerializer(classOf[PhoneNumber], new PhoneNumberSerializer)

    //    module.addSerializer(classOf[Country], new SimpleCountrySerializer)

    module.addDeserializer(classOf[RealNameInfo], new RealNameInfoDeserializer())
    module.addDeserializer(classOf[IdProof], new IdProofDeserializer())
    //    module.addDeserializer(classOf[Country], new SimpleCountryDeserializer())
    module.addDeserializer(classOf[PhoneNumber], new PhoneNumberDerializer())

    mapper.registerModule(module)
    mapper
  }

}

object TravellersFormatter {
  lazy val instance = new TravellersFormatter
}
