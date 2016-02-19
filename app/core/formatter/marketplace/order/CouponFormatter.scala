package core.formatter.marketplace.order

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.lvxingpai.model.marketplace.misc.Coupon
import core.formatter.BaseFormatter

/**
 * Created by topy on 2016/2/19.
 */
class CouponFormatter extends BaseFormatter {

  override protected val objectMapper = {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)
    val module = new SimpleModule()
    module.addSerializer(classOf[Coupon], new CouponSerializer)
    mapper.registerModule(module)
    mapper
  }
}

object CouponFormatter {
  lazy val instance = new CouponFormatter
}