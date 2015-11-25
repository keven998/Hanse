package core.formatter.marketplace.product

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.account.UserInfo
import com.lvxingpai.model.geo.Locality
import com.lvxingpai.model.marketplace.product.Commodity
import com.lvxingpai.model.marketplace.seller.Seller
import com.lvxingpai.model.misc.ImageItem
import core.formatter.BaseFormatter
import core.formatter.geo.SimpleLocalitySerializer
import core.formatter.marketplace.seller.SimpleSellerSerializer
import core.formatter.misc.ImageItemSerializer
import core.formatter.user.UserSerializer

/**
 * Created by pengyt on 2015/11/13.
 */
class SimpleCommodityFormatter extends BaseFormatter {

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    mapper.registerModule(DefaultScalaModule)
    module.addSerializer(classOf[Commodity], new SimpleCommoditySerializer)
    module.addSerializer(classOf[ImageItem], new ImageItemSerializer)
    module.addSerializer(classOf[Seller], new SimpleSellerSerializer)
    module.addSerializer(classOf[UserInfo], new UserSerializer)
    module.addSerializer(classOf[Locality], new SimpleLocalitySerializer)
    mapper.registerModule(module)
    mapper
  }
}