package core.formatter.marketplace.product

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.account.UserInfo
import com.lvxingpai.model.geo.{ Country, Locality }
import com.lvxingpai.model.marketplace.product.{ Pricing, StockInfo, CommodityPlan, Commodity }
import com.lvxingpai.model.marketplace.seller.{ BankAccount, Seller }
import com.lvxingpai.model.misc.{ ImageItem, PhoneNumber, RichText }
import core.formatter.BaseFormatter
import core.formatter.geo.{ SimpleCountrySerializer, SimpleLocalitySerializer }
import core.formatter.marketplace.seller.{ BankAccountSerializer, SimpleSellerSerializer }
import core.formatter.misc.{ ImageItemSerializer, PhoneNumberSerializer, RichTextSerializer }
import core.formatter.user.UserSerializer

/**
 * Created by pengyt on 2015/11/3.
 */
class CommodityFormatter extends BaseFormatter {

  override protected val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    mapper.registerModule(DefaultScalaModule)
    module.addSerializer(classOf[RichText], new RichTextSerializer)
    module.addSerializer(classOf[Commodity], new CommoditySerializer)
    module.addSerializer(classOf[CommodityPlan], new CommodityPlanSerializer)
    module.addSerializer(classOf[StockInfo], new StockInfoSerializer)
    module.addSerializer(classOf[BankAccount], new BankAccountSerializer)
    module.addSerializer(classOf[Seller], new SimpleSellerSerializer)
    module.addSerializer(classOf[PhoneNumber], new PhoneNumberSerializer)
    module.addSerializer(classOf[UserInfo], new UserSerializer)
    module.addSerializer(classOf[ImageItem], new ImageItemSerializer)
    module.addSerializer(classOf[Locality], new SimpleLocalitySerializer)
    module.addSerializer(classOf[Country], new SimpleCountrySerializer)
    module.addSerializer(classOf[Pricing], new PricingSerializer)
    mapper.registerModule(module)
    mapper
  }
}

object CommodityFormatter {
  lazy val instance = new CommodityFormatter
}
