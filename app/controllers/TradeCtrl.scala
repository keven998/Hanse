package controllers

import javax.inject._

import com.fasterxml.jackson.databind.JsonNode
import core.api.OrderAPI
import core.misc.HanseResult
import org.bson.types.ObjectId
import play.api.mvc.{ Action, Controller, Result }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import core.Implicits._

/**
 * Created by topy on 2015/10/22.
 */
@Singleton
class TradeCtrl extends Controller {

  def createOrder() = Action.async(
    request => {
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
      null
    }
  )
}
