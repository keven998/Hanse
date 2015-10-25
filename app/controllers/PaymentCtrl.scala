package controllers

import javax.inject.Singleton

import core.Implicits._
import core.api.OrderAPI
import core.misc.HanseResult
import core.model.trade.order.Order
import org.bson.types.ObjectId
import play.api.mvc.{ Action, Controller }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PaymentCtrl extends Controller {

  def createPayments() = Action.async(
    request => {
      Future {
        val ret = for {
          body <- request.body.asJson
          orderId <- (body \ "orderId").asOpt[String]
          ip = (body \ "ip").asOpt[String].getOrElse("")
          tradeType = (body \ "tradeType").asOpt[String].getOrElse("")
          vendor = (body \ "vendor").asOpt[String].getOrElse("")
        } yield {
          for {
            orderValue <- OrderAPI.getOrder(new ObjectId(orderId))
          } yield {
            orderValue
            null
          }
        }
        ret getOrElse Future(HanseResult.unprocessable())
      }
      null
    }
  )

}
