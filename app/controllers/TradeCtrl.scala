package controllers

import javax.inject._

import com.fasterxml.jackson.databind.ObjectMapper
import core.api.{ CommodityAPI, OrderAPI }
import core.misc.HanseResult
import core.model.trade.order.Order
import play.api.mvc.{ Action, Controller }

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by topy on 2015/10/22.
 */
@Singleton
class TradeCtrl extends Controller {

  /**
   * 创建商品
   * @return
   */
  def createCommodity() = Action.async(
    request => {
      val futureCmy = for {
        body <- request.body.asJson
        salerId <- (body \ "salerId").asOpt[Long]
        title <- (body \ "title").asOpt[String]
        detail <- (body \ "detail").asOpt[String]
        price <- (body \ "price").asOpt[Float]
      } yield {
        CommodityAPI.addCommodity(salerId, title, detail, price)
      }
      val mapper = new ObjectMapper()
      val node = mapper.createObjectNode()
      for {
        cmy <- futureCmy.get
      } yield {
        node.put("commodityId", cmy.id.toString)
        node.put("title", cmy.title)
        HanseResult(data = Some(node))
      }
    })

  /**
   * 创建订单
   * @return 返回订单信息
   */
  def createOrder() = Action.async(
    request => {
      val cmyPara = for {
        body <- request.body.asJson
        commodityId <- (body \ "commodityId").asOpt[String]
        quantity <- (body \ "quantity").asOpt[Int]
      } yield commodityId -> quantity

      val mapper = new ObjectMapper()
      val node = mapper.createObjectNode()

      if (cmyPara isEmpty) Future { HanseResult.unprocessable() }
      else {
        for {
          order <- OrderAPI.addOrder(cmyPara.get._1, cmyPara.get._2)
        } yield {
          node.put("orderId", order.id.toString)
          node.put("cmyTitle", order.commodity.title)
          node.put("cmyPrice", order.commodity.price)
          node.put("cmyDetail", order.commodity.detail)
          //          node.put("salerName", order.commodity.saler.realNameInfo.givenName)
          node.put("discount", order.discount)
          node.put("quantity", order.quantity)
          node.put("status", order.status)
          node.put("totalPrice", order.totalPrice)
          node.put("orderTime", order.orderTime)
          HanseResult(data = Some(node))
        }
      }
    })

  /**
   * 预支付
   * @return 带签名的字符串
   */
  def prePay() = Action.async(
    request => {
      val orderPara = for {
        body <- request.body.asJson
        orderId <- (body \ "orderId").asOpt[String]
        payType <- (body \ "payType").asOpt[String]
      } yield orderId -> payType

      val mapper = new ObjectMapper()
      val node = mapper.createObjectNode()

      if (orderPara isEmpty) Future { HanseResult.unprocessable() }
      else {
        for {
          str <- OrderAPI.prePay(orderPara.get._2, orderPara.get._1)
        } yield {
          node.put("result", str)
          HanseResult(data = Some(node))
        }
      }
    })
}
