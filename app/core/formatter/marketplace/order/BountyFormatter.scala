package core.formatter.marketplace.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.account.{ RealNameInfo, UserInfo }
import com.lvxingpai.model.geo.Locality
import com.lvxingpai.model.marketplace.order.Bounty
import com.lvxingpai.model.marketplace.product.Schedule
import com.lvxingpai.model.marketplace.seller.Seller
import com.lvxingpai.model.misc.{ ImageItem, PhoneNumber, RichText }
import core.formatter.BaseFormatter
import core.formatter.geo.SimpleLocalitySerializer
import core.formatter.marketplace.seller.MiniSellerSerializer
import core.formatter.misc._
import core.formatter.user.UserSerializer

/**
 * Created by topy on 2016/3/30.
 */
class BountyFormatter extends BaseFormatter {

  override protected val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    mapper.registerModule(DefaultScalaModule)
    module.addSerializer(classOf[Bounty], new BountySerializer)
    module.addSerializer(classOf[Seller], new MiniSellerSerializer)
    module.addSerializer(classOf[RichText], new RichTextSerializer)
    module.addSerializer(classOf[PhoneNumber], new PhoneNumberSerializer)
    module.addSerializer(classOf[UserInfo], new UserSerializer)
    module.addSerializer(classOf[ImageItem], new ImageItemSerializer)
    module.addSerializer(classOf[RealNameInfo], new RealNameInfoSerializer)
    module.addSerializer(classOf[Locality], new SimpleLocalitySerializer)
    module.addSerializer(classOf[Schedule], new ScheduleSerializer)
    module.addSerializer(classOf[Seller], new MiniSellerSerializer)
    mapper.registerModule(module)
    mapper
  }
}

object BountyFormatter {
  lazy val instance = new BountyFormatter
}
