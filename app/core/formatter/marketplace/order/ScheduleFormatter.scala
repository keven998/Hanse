package core.formatter.marketplace.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.account.UserInfo
import com.lvxingpai.model.guide.Guide
import com.lvxingpai.model.marketplace.product.Schedule
import com.lvxingpai.model.marketplace.seller.Seller
import core.formatter.BaseFormatter
import core.formatter.marketplace.seller.MiniSellerSerializer
import core.formatter.user.UserSerializer

/**
 * Created by topy on 2016/3/31.
 */
class ScheduleFormatter extends BaseFormatter {

  override protected val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    mapper.registerModule(DefaultScalaModule)
    module.addSerializer(classOf[Schedule], new ScheduleSerializer)
    module.addSerializer(classOf[Seller], new MiniSellerSerializer)
    module.addSerializer(classOf[UserInfo], new UserSerializer)
    module.addSerializer(classOf[Guide], new GuideSerializer)
    mapper.registerModule(module)
    mapper
  }
}

object ScheduleFormatter {
  lazy val instance = new ScheduleFormatter
}

