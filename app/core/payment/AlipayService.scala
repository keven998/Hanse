package core.payment

import javax.inject.Inject

import com.lvxingpai.inject.morphia.MorphiaMap
import core.payment.PaymentService.Provider
import org.mongodb.morphia.Datastore
import play.api.Play
import play.api.Play.current

/**
 * 支付宝的相关接口
 *
 * Created by zephyre on 12/16/15.
 */
class AlipayService @Inject() (private val m: MorphiaMap) extends PaymentService {

  override lazy val datastore: Datastore = m.map("k2")

  override lazy val provider: Provider.Value = Provider.Alipay
}

object AlipayService {
  lazy val instance = Play.application.injector.instanceOf[AlipayService]
}
