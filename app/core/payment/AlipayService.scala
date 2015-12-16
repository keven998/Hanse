package core.payment

import javax.inject.Inject

import com.lvxingpai.inject.morphia.MorphiaMap
import play.api.Play
import play.api.Play.current

/**
 * 支付宝的相关接口
 *
 * Created by zephyre on 12/16/15.
 */
class AlipayService @Inject() (private val m: MorphiaMap) extends PaymentService {
  override val morphiaMap: MorphiaMap = m
}

object AlipayService {
  lazy val instance = Play.application.injector.instanceOf[AlipayService]
}
