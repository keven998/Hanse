package core.formatter.marketplace.seller

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.marketplace.product.{ Commodity, CommodityPlan, Pricing, StockInfo }
import com.lvxingpai.model.marketplace.seller.{ BankAccount, Seller }
import com.lvxingpai.model.misc.PhoneNumber
import com.lvxingpai.yunkai.UserInfo
import core.formatter.BaseFormatter
import core.formatter.marketplace.product.{ CommodityPlanSerializer, CommoditySerializer, PricingSerializer, StockInfoSerializer }
import core.formatter.misc.PhoneNumberSerializer
import core.formatter.user.UserSerializer

/**
 * Created by pengyt on 2015/11/3.
 */
class SellerFormatter extends BaseFormatter {
  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    mapper.registerModule(DefaultScalaModule)
    module.addSerializer(classOf[CommodityPlan], new CommodityPlanSerializer)
    module.addSerializer(classOf[Commodity], new CommoditySerializer)
    module.addSerializer(classOf[Pricing], new PricingSerializer)
    module.addSerializer(classOf[StockInfo], new StockInfoSerializer)
    module.addSerializer(classOf[BankAccount], new BankAccountSerializer)
    module.addSerializer(classOf[Seller], new SellerSerializer)
    module.addSerializer(classOf[UserInfo], new UserSerializer)
    module.addSerializer(classOf[PhoneNumber], new PhoneNumberSerializer)
    mapper.registerModule(module)
    mapper
  }
}

