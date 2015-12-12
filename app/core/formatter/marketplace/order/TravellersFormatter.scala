package core.formatter.marketplace.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.account.{ IdProof, RealNameInfo }
import com.lvxingpai.model.misc.PhoneNumber
import core.formatter.BaseFormatter
import core.formatter.misc.{ IdProofSerializer, PhoneNumberSerializer }

/**
 * Created by pengyt on 2015/11/21.
 */
class TravellersFormatter extends BaseFormatter {

  override protected val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    mapper.registerModule(DefaultScalaModule)
    module.addSerializer(classOf[RealNameInfo], new ContactAndTravellersSerializer)
    module.addSerializer(classOf[IdProof], new IdProofSerializer)
    module.addSerializer(classOf[PhoneNumber], new PhoneNumberSerializer)
    mapper.registerModule(module)
    mapper
  }

}

object TravellersFormatter {
  lazy val instance = new TravellersFormatter
}
