package controllers

import javax.inject._

import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper }
import com.lvxingpai.inject.morphia.MorphiaMap
import core.api.{ CommodityAPI, OrderAPI }
import core.formatter.marketplace.order.OrderFormatter
import core.misc.HanseResult
import play.api.Configuration
import play.api.mvc.{ Action, Controller, Results }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by topy on 2015/10/22.
 */
@Singleton
class TradeCtrl @Inject() (@Named("default") configuration: Configuration, datastore: MorphiaMap) extends Controller {

  implicit lazy val ds = datastore.map.get("k2").get

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
    }
  )

  /**
   * 创建订单
   * @return 返回订单信息
   */
  def createOrder() = Action.async(
    request => {
      val cmyPara = for {
        body <- request.body.asJson
        commodityId <- (body \ "commodityId").asOpt[Long]
        planId <- (body \ "planId").asOpt[String]
        rendezvousTime <- (body \ "rendezvousTime").asOpt[String]
        quantity <- (body \ "quantity").asOpt[Int]
        //        travellers <- (body \ "travellers").asOpt[Array[Person]]
        name <- (body \ "name").asOpt[Seq[String]]
        phone <- (body \ "contact.phone").asOpt[String]
        email <- (body \ "contact.email").asOpt[String]
        address <- (body \ "contact.address").asOpt[String]
        comment <- (body \ "contact.comment").asOpt[String]
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
          node.put("cmyDetail", order.commodity.desc.summary)
          //          node.put("salerName", order.commodity.saler.realNameInfo.givenName)
          node.put("discount", order.discount)
          node.put("quantity", order.quantity)
          node.put("status", order.status)
          node.put("totalPrice", order.totalPrice)
          node.put("orderTime", order.createTime.toString)
          HanseResult(data = Some(node))
        }
      }
    }
  )

  /**
   * 预支付
   * @return 带签名的字符串
   */
  def prePay() = Action.async(
    request => {
      val orderPara = for {
        body <- request.body.asJson
        orderId <- (body \ "orderId").asOpt[Long]
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
    }
  )

  /**
   * 根据订单号查询订单的支付结果, 如果支付成功, 直接返回, 如果不成功, 主动请求支付宝查询接口, 如果支付成功则修改订单状态, 否则直接返回未支付
   * @return 订单状态
   */
  def payConfirm() = Action.async(
    request => {
      val futureStatus = for {
        body <- request.body.asJson
        orderId <- (body \ "orderId").asOpt[Long]
      } yield OrderAPI.getOrderStatus(orderId)

      val mapper = new ObjectMapper()
      val node = mapper.createObjectNode()
      for {
        status <- futureStatus.get
      } yield {
        node.put("status", status)
        HanseResult(data = Some(node))
      }
    }
  )

  /**
   * 支付宝回调接口
   * @return 支付宝需要的结果：success
   */
  def notifyResult() = Action.async(
    request => {
      Future {
        // 获取回调数据
        val dataOpt = request.body.asFormUrlEncoded
        if (dataOpt.nonEmpty) {
          val data = dataOpt.get
          // 将获取的数据按字典排序
          val sortedKeys = data.keys.toSeq.sorted
          // 剔除"sign", "sign_type"字段, 将数据组装成所需的字符串
          val contents = sortedKeys filter (key => !(Seq("sign", "sign_type") contains key)) map (key => {
            val value = data(key) mkString ""
            s"$key=$value"
          }) mkString "&"
          // 支付宝签名
          val sign = data("sign").head
          // 订单信息
          val out_trade_no = data("out_trade_no").head.toLong
          // 订单状态
          val trade_status = data("trade_status").head
          // 验证支付宝签名
          // 支付宝公钥
          if (OrderAPI.verifyAlipay(contents, sign)) {
            // 系统的订单状态
            val orderStatus = OrderAPI.aliOrderStatus2OrderStatus(trade_status)
            // 验证通过
            // 根据支付宝的回调结果修改订单状态
            OrderAPI.updateOrderStatus(out_trade_no, orderStatus)
            // 返回支付宝信息
            Results.Ok("success")
          } else {
            HanseResult.forbidden(HanseResult.RetCode.FORBIDDEN, errorMsg = Some("invaild request"))
          }
        } else {
          HanseResult.forbidden(HanseResult.RetCode.FORBIDDEN, errorMsg = Some("invaild request"))
        }
      }
    }
  )

  /**
   * 根据用户id获取订单列表
   * 如果订单状态为空, 获取所在用户下的所有的订单列表
   * 如果订单状态不为空, 获取所在用户下的某个订单状态的订单列表
   * @param userId 用户id
   * @param status 订单状态
   * @return 订单列表
   */
  def getOrders(userId: Long, status: Option[String]) = Action.async(
    request => {
      val orderMapper = new OrderFormatter().objectMapper
      for {
        orders <- OrderAPI.getOrderList(userId, status)
      } yield {
        val node = orderMapper.valueToTree[JsonNode](orders)
        HanseResult(data = Some(node))
      }
    }
  )

  /**
   * 订单详情
   * @param orderId 订单id
   * @return 订单详情
   */
  def getOrderInfo(orderId: Long) = Action.async(
    request => {
      val orderMapper = new OrderFormatter().objectMapper
      for {
        order <- OrderAPI.getOrder(orderId)
      } yield {
        val node = orderMapper.valueToTree[JsonNode](order)
        HanseResult(data = Some(node))
      }
    }
  )
}
