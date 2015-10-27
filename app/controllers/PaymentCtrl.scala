package controllers

import javax.inject.Singleton

import core.api.OrderAPI
import core.misc.HanseResult
import core.misc.Implicits._
import core.service.PaymentService
import org.bson.types.ObjectId
import play.api.mvc.{ Action, Controller }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.Elem

@Singleton
class PaymentCtrl extends Controller {

  def createPayments(orderId: String) = Action.async(
    request => {
      Future {
        val ret = for {
          body <- request.body.asJson
          userId = request.headers.get("UserId") map (_.toLong)
          name = (body \ "name").asOpt[String].getOrElse("")
          ip = (body \ "ip").asOpt[String].getOrElse("")
          tradeType = (body \ "tradeType").asOpt[String].getOrElse("")
          vendor = (body \ "vendor").asOpt[String].getOrElse("")
        } yield {
          for {
            orderValue <- OrderAPI.getOrder(new ObjectId(orderId))
            wcResponse <- PaymentService.unifiedOrder(
              Map("out_trade_no" -> orderId, "openid" -> userId.getOrElse(0).toString,
                "attach" -> "微信支付", "ip" -> ip, "body" -> name,
                "trade_type" -> tradeType, "total_fee" -> orderValue.getTotalPrice * 100))
          } yield {
            val node = wcResponse.asInstanceOf[Elem]

            null
          }
        }
        ret getOrElse Future(HanseResult.unprocessable())
      }
      null
    }
  )

}
