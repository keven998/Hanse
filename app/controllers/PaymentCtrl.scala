package controllers

import javax.inject.Singleton

import core.misc.HanseResult
import core.misc.Implicits._
import core.service.PaymentService
import play.api.mvc.{ Action, Controller }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class PaymentCtrl extends Controller {

  def createPayments(orderId: String) = Action.async(
    request => {
      val ret = for {
        body <- request.body.asJson
        userId = request.headers.get("UserId") map (_.toLong)
        name = (body \ "name").asOpt[String].getOrElse("")
        ip = (body \ "ip").asOpt[String].getOrElse("")
        tradeType = (body \ "tradeType").asOpt[String].getOrElse("")
        vendor = (body \ "vendor").asOpt[String].getOrElse("")
      } yield {
        for {
          //orderValue <- OrderAPI.getOrder(new ObjectId(orderId))
          wcResponse <- PaymentService.unifiedOrder(
            Map("out_trade_no" -> orderId,
              "attach" -> "微信支付", "spbill_create_ip" -> ip, "body" -> "Onetemp",
              "trade_type" -> tradeType, "total_fee" -> 100 * 100))
        } yield {
          //val node = wcResponse.asInstanceOf[Elem]
          val str = new String(wcResponse.bodyAsBytes, "UTF8")
          Ok(str)
        }
      }
      ret getOrElse Future {
        HanseResult.unprocessable()
      }
    }
  )

  def wechatCallback() = Action.async(
    request => {
      val ret = for {
        body <- request.body.asJson
        userId = request.headers.get("UserId") map (_.toLong)
        name = (body \ "name").asOpt[String].getOrElse("")
        ip = (body \ "ip").asOpt[String].getOrElse("")
        tradeType = (body \ "tradeType").asOpt[String].getOrElse("")
        vendor = (body \ "vendor").asOpt[String].getOrElse("")
      } yield {
        //          for {
        //            orderValue <- OrderAPI.getOrder(new ObjectId(orderId))
        //            wcResponse <- PaymentService.unifiedOrder(
        //              Map("out_trade_no" -> orderId, "openid" -> userId.getOrElse(0).toString,
        //                "attach" -> "微信支付", "ip" -> ip, "body" -> "One temp",
        //                "trade_type" -> tradeType, "total_fee" -> 100 * 100))
        //          } yield {
        //            //val node = wcResponse.asInstanceOf[Elem]
        //            HanseResult.ok(RetCode.OK, None, Some(wcResponse))
        //          }
        null
      }
      ret getOrElse Future {
        HanseResult.unprocessable()
      }
    }
  )

}
