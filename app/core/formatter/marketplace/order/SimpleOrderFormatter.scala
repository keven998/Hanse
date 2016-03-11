package core.formatter.marketplace.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.account.{ RealNameInfo, UserInfo }
import com.lvxingpai.model.marketplace.order.{ OrderActivity, Order }
import com.lvxingpai.model.marketplace.product.{ BasicCommodity, CommodityPlan, Pricing }
import com.lvxingpai.model.marketplace.seller.Seller
import com.lvxingpai.model.misc.{ ImageItem, RichText }
import core.formatter.BaseFormatter
import core.formatter.marketplace.product.{ CommodityPlanSerializer, CommoditySnapsSerializer, PricingSerializer }
import core.formatter.marketplace.seller.MiniSellerSerializer
import core.formatter.misc._
import core.formatter.user.UserSerializer

/**
 * Created by pengyt on 2015/11/21.
 */
class SimpleOrderFormatter extends BaseFormatter {

  override protected val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    mapper.registerModule(DefaultScalaModule)
    module.addSerializer(classOf[Order], new SimpleOrderSerializer)
    module.addSerializer(classOf[BasicCommodity], new CommoditySnapsSerializer)
    module.addSerializer(classOf[Seller], new MiniSellerSerializer)
    module.addSerializer(classOf[CommodityPlan], new CommodityPlanSerializer)
    module.addSerializer(classOf[RichText], new RichTextSerializer)
    module.addSerializer(classOf[UserInfo], new UserSerializer)
    module.addSerializer(classOf[ImageItem], new ImageItemSerializer)
    module.addSerializer(classOf[Pricing], new PricingSerializer)
    module.addSerializer(classOf[RealNameInfo], new RealNameInfoSerializer)
    module.addSerializer(classOf[OrderActivity], new OrderActivitySerializer)
    mapper.registerModule(module)
    mapper
  }
}

object SimpleOrderFormatter {
  lazy val instance = new SimpleOrderFormatter
}
