package core.formatter.marketplace.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.account.{ IdProof, RealNameInfo, UserInfo }
import com.lvxingpai.model.geo.Country
import com.lvxingpai.model.marketplace.order.{ Order, OrderActivity }
import com.lvxingpai.model.marketplace.product.{ BasicCommodity, CommodityPlan, Pricing }
import com.lvxingpai.model.marketplace.seller.{ BankAccount, Seller }
import com.lvxingpai.model.misc.{ ImageItem, PhoneNumber, RichText }
import core.formatter.BaseFormatter
import core.formatter.geo.SimpleCountrySerializer
import core.formatter.marketplace.product.{ CommoditySnapsSerializer, PricingSerializer, SimpleCommodityPlanSerializer }
import core.formatter.marketplace.seller.{ BankAccountSerializer, MiniSellerSerializer }
import core.formatter.misc._
import core.formatter.user.UserSerializer

/**
 * Created by pengyt on 2015/11/21.
 */
class OrderFormatter extends BaseFormatter {

  override protected val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    mapper.registerModule(DefaultScalaModule)
    module.addSerializer(classOf[Order], new OrderSerializer)
    module.addSerializer(classOf[BasicCommodity], new CommoditySnapsSerializer)
    module.addSerializer(classOf[Seller], new MiniSellerSerializer)
    module.addSerializer(classOf[CommodityPlan], new SimpleCommodityPlanSerializer)
    module.addSerializer(classOf[RichText], new RichTextSerializer)
    module.addSerializer(classOf[BankAccount], new BankAccountSerializer)
    module.addSerializer(classOf[PhoneNumber], new PhoneNumberSerializer)
    module.addSerializer(classOf[UserInfo], new UserSerializer)
    module.addSerializer(classOf[ImageItem], new ImageItemSerializer)
    module.addSerializer(classOf[RealNameInfo], new RealNameInfoSerializer)
    module.addSerializer(classOf[IdProof], new IdProofSerializer)
    module.addSerializer(classOf[Country], new SimpleCountrySerializer)
    module.addSerializer(classOf[OrderActivity], new OrderActivitySerializer)
    module.addSerializer(classOf[Pricing], new PricingSerializer)
    mapper.registerModule(module)
    mapper
  }
}

object OrderFormatter {
  lazy val instance = new OrderFormatter
}
