package core.formatter.marketplace.seller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.account.UserInfo
import com.lvxingpai.model.geo.GeoEntity
import com.lvxingpai.model.marketplace.seller.Seller
import com.lvxingpai.model.misc.{ ImageItem, RichText, PhoneNumber }
import core.formatter.BaseFormatter
import core.formatter.geo.SimpleLocalitySerializer
import core.formatter.misc.{ ImageItemSerializer, RichTextSerializer, PhoneNumberSerializer }
import core.formatter.user.UserSerializer

/**
 * Created by topy on 2015/11/3.
 */
class SellerFormatter extends BaseFormatter {
  override protected val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    mapper.registerModule(DefaultScalaModule)
    module.addSerializer(classOf[Seller], new SellerSerializer)
    module.addSerializer(classOf[ImageItem], new ImageItemSerializer)
    module.addSerializer(classOf[UserInfo], new UserSerializer)
    module.addSerializer(classOf[RichText], new RichTextSerializer)
    module.addSerializer(classOf[PhoneNumber], new PhoneNumberSerializer)
    module.addSerializer(classOf[GeoEntity], new SimpleLocalitySerializer)
    mapper.registerModule(module)
    mapper
  }
}

object SellerFormatter {
  lazy val instance = new SellerFormatter
}
