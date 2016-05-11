package core.formatter.geo

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.account.UserInfo
import com.lvxingpai.model.marketplace.seller.Seller
import com.lvxingpai.model.misc.ImageItem
import core.formatter.BaseFormatter
import core.formatter.marketplace.seller.MiniSellerSerializer
import core.formatter.misc.ImageItemSerializer
import core.formatter.user.UserSerializer
import core.model.misc.GeoCommodity

/**
 * Created by topy on 2016/2/19.
 */
class GeoCommodityFormatter extends BaseFormatter {

  override protected val objectMapper = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    val module = new SimpleModule()
    module.addSerializer(classOf[GeoCommodity], new GeoCommoditySerializer)
    module.addSerializer(classOf[ImageItem], new ImageItemSerializer)
    module.addSerializer(classOf[UserInfo], new UserSerializer)
    module.addSerializer(classOf[Seller], new MiniSellerSerializer)
    mapper.registerModule(module)
    mapper
  }
}

object GeoCommodityFormatter {
  lazy val instance = new GeoCommodityFormatter
}

