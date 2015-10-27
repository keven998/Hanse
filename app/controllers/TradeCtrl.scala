package controllers

import javax.inject._

import com.fasterxml.jackson.databind.ObjectMapper
import core.api.{ CommodityAPI, OrderAPI }
import core.misc.HanseResult
import core.model.trade.order.Order
import org.bson.types.ObjectId
import play.api.mvc.{ Results, Action, Controller }

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

  /**
   * 根据订单号查询订单的支付结果, 如果支付成功, 直接返回, 如果不成功, 主动请求支付宝查询接口, 如果支付成功则修改订单状态, 否则直接返回未支付
   * @return 订单状态
   */
  def payConfirm() = Action.async(
    request => {
      val futureStatus = for {
        body <- request.body.asJson
        orderId <- (body \ "orderId").asOpt[String]
      } yield OrderAPI.getOrderStatus(orderId)

      val mapper = new ObjectMapper()
      val node = mapper.createObjectNode()
      for {
        status <- futureStatus.get
      } yield {
        node.put("status", status)
        HanseResult(data = Some(node))
      }
    })

  /**
   * 支付宝回调接口
   * @return 支付宝需要的结果
   */
  def notifyResult() = Action.async(
    request => {

      val queryStr = request.queryString
      val notify_time = queryStr.get("notify_time")
      val ret = if (notify_time isEmpty) {
        for {
          body <- request.body.asJson
          notify_time <- (body \ "notify_time").asOpt[String]
          notify_type <- (body \ "notify_type").asOpt[String]
          notify_id <- (body \ "notify_id").asOpt[String]
          sign_type <- (body \ "sign_type").asOpt[String]
          sign <- (body \ "sign").asOpt[String]
          out_trade_no <- (body \ "out_trade_no").asOpt[String]
          subject <- (body \ "subject").asOpt[String]
          payment_type <- (body \ "payment_type").asOpt[String]
          trade_no <- (body \ "trade_no").asOpt[String]
          trade_status <- (body \ "trade_status").asOpt[String]
          seller_id <- (body \ "seller_id").asOpt[String]
          seller_email <- (body \ "seller_email").asOpt[String]
          buyer_id <- (body \ "buyer_id").asOpt[String]
          buyer_email <- (body \ "buyer_email").asOpt[String]
          total_fee <- (body \ "total_fee").asOpt[String]
          quantity <- (body \ "quantity").asOpt[String]
          price <- (body \ "price").asOpt[String]
          body1 <- (body \ "body").asOpt[String]
          gmt_create <- (body \ "gmt_create").asOpt[String]
          gmt_payment <- (body \ "gmt_payment").asOpt[String]
          is_total_fee_adjust <- (body \ "is_total_fee_adjust").asOpt[String]
          use_coupon <- (body \ "use_coupon").asOpt[String]
          discount <- (body \ "discount").asOpt[String]
          refund_status <- (body \ "refund_status").asOpt[String]
          gmt_refund <- (body \ "gmt_refund").asOpt[String]
        } yield {
          val body_str = "body=" + (if (body1 != null) body1 else "")
          val buyer_id_str = "&buyer_id=" + (if (buyer_id != null) buyer_id else "")
          val buyer_email_str = "&buyer_email=" + (if (buyer_email != null) buyer_email else "")
          val discount_str = "&discount=" + (if (discount != null) discount else "")
          val gmt_create_str = "&gmt_create=" + (if (gmt_create != null) gmt_create else "")
          val gmt_payment_str = "&gmt_payment=" + (if (gmt_payment != null) gmt_payment else "")
          val gmt_refund_str = "&gmt_refund=" + (if (gmt_refund != null) gmt_refund else "")
          val is_total_fee_adjust_str = "&is_total_fee_adjust=" + (if (is_total_fee_adjust != null) is_total_fee_adjust else "")
          val notify_id_str = "&notify_id=" + (if (notify_id != null) notify_id else "")
          val notify_time_str = "&notify_time=" + (if (notify_time != null) notify_time else "")
          val notify_type_str = "&notify_type=" + (if (notify_type != null) notify_type else "")
          val out_trade_no_str = "&out_trade_no=" + (if (out_trade_no != null) out_trade_no else "")
          val payment_type_str = "&payment_type=" + (if (payment_type != null) payment_type else "")
          val price_str = "&price=" + (if (price != null) price else "")
          val quantity_str = "&quantity=" + (if (quantity != null) quantity else "")
          val refund_status_str = "&refund_status=" + (if (refund_status != null) refund_status else "")
          val seller_id_str = "&seller_id=" + (if (seller_id != null) seller_id else "")
          val seller_email_str = "&seller_email=" + (if (seller_email != null) seller_email else "")
          val subject_str = "&subject=" + (if (subject != null) subject else "")
          val total_fee_str = "&total_fee=" + (if (total_fee != null) total_fee else "")
          val trade_no_str = "&trade_no=" + (if (trade_no != null) trade_no else "")
          val trade_status_str = "&trade_status=" + (if (trade_status != null) trade_status else "")
          val use_coupon_str = "&use_coupon=" + (if (use_coupon != null) use_coupon else "")

          // 验证支付宝签名
          // 支付宝公钥
          val content = body_str + buyer_id_str + buyer_email_str + discount_str + gmt_create_str + gmt_payment_str +
            gmt_refund_str + is_total_fee_adjust_str + notify_id_str + notify_time_str + notify_type_str + out_trade_no_str +
            payment_type_str + price_str + quantity_str + refund_status_str + seller_id_str + seller_email_str + subject_str +
            total_fee_str + trade_no_str + trade_status_str + use_coupon_str

          val discount_str1 = "discount=" + (if (discount != null) discount else "")

          val content1 = discount_str1 + payment_type_str + subject_str + trade_no_str + buyer_email_str + gmt_create_str + notify_type_str +
            quantity_str + out_trade_no_str + seller_id_str + notify_time_str + "&" + body_str + trade_status_str + is_total_fee_adjust_str +
            total_fee_str + gmt_payment_str + seller_email_str + price_str + buyer_id_str + notify_id_str + use_coupon_str

          if (OrderAPI.verifyAlipay(content, sign)) { // 验证通过
            // 根据支付宝的回调结果修改订单状态
            OrderAPI.updateOrderStatus(out_trade_no, trade_status)
            // 返回支付宝信息
            Results.Ok("success")
          } else {
            HanseResult.forbidden(HanseResult.RetCode.FORBIDDEN, errorMsg = Some("invaild request"))
          }
        }
      } else {
        val body_str = "body=" + (if (queryStr.get("body") nonEmpty) queryStr.get("body").get.head else "")
        val buyer_id_str = "&buyer_id=" + (if (queryStr.get("buyer_id") nonEmpty) queryStr.get("buyer_id").get.head else "")
        val buyer_email_str = "&buyer_email=" + (if (queryStr.get("buyer_email") nonEmpty) queryStr.get("buyer_email").get.head else "")
        val discount_str = "&discount=" + (if (queryStr.get("discount") nonEmpty) queryStr.get("discount").get.head else "")
        val gmt_create_str = "&gmt_create=" + (if (queryStr.get("gmt_create") nonEmpty) queryStr.get("gmt_create").get.head else "")
        val gmt_payment_str = "&gmt_payment=" + (if (queryStr.get("gmt_payment") nonEmpty) queryStr.get("gmt_payment").get.head else "")
        val gmt_refund_str = "&gmt_refund=" + (if (queryStr.get("gmt_refund") nonEmpty) queryStr.get("gmt_refund").get.head else "")
        val is_total_fee_adjust_str = "&is_total_fee_adjust=" + (if (queryStr.get("is_total_fee_adjust") nonEmpty) queryStr.get("is_total_fee_adjust").get.head else "")
        val notify_id_str = "&notify_id=" + (if (queryStr.get("notify_id") nonEmpty) queryStr.get("notify_id").get.head else "")
        val notify_time_str = "&notify_time=" + (if (queryStr.get("notify_time") nonEmpty) queryStr.get("notify_time").get.head else "")
        val notify_type_str = "&notify_type=" + (if (queryStr.get("notify_type") nonEmpty) queryStr.get("notify_type").get.head else "")
        val out_trade_no_str = "&out_trade_no=" + (if (queryStr.get("out_trade_no") nonEmpty) queryStr.get("out_trade_no").get.head else "")
        val payment_type_str = "&payment_type=" + (if (queryStr.get("payment_type") nonEmpty) queryStr.get("payment_type").get.head else "")
        val price_str = "&price=" + (if (queryStr.get("price") nonEmpty) queryStr.get("price").get.head else "")
        val quantity_str = "&quantity=" + (if (queryStr.get("quantity") nonEmpty) queryStr.get("quantity").get.head else "")
        val refund_status_str = "&refund_status=" + (if (queryStr.get("refund_status") nonEmpty) queryStr.get("refund_status").get.head else "")
        val seller_id_str = "&seller_id=" + (if (queryStr.get("seller_id") nonEmpty) queryStr.get("seller_id").get.head else "")
        val seller_email_str = "&seller_email=" + (if (queryStr.get("seller_email") nonEmpty) queryStr.get("seller_email").get.head else "")
        val subject_str = "&subject=" + (if (queryStr.get("subject") nonEmpty) queryStr.get("subject").get.head else "")
        val total_fee_str = "&total_fee=" + (if (queryStr.get("total_fee") nonEmpty) queryStr.get("total_fee").get.head else "")
        val trade_no_str = "&trade_no=" + (if (queryStr.get("trade_no") nonEmpty) queryStr.get("trade_no").get.head else "")
        val trade_status_str = "&trade_status=" + (if (queryStr.get("trade_status") nonEmpty) queryStr.get("trade_status").get.head else "")
        val use_coupon_str = "&use_coupon=" + (if (queryStr.get("use_coupon") nonEmpty) queryStr.get("use_coupon").get.head else "")
        val sign = queryStr.get("sign").get.head
        val out_trade_no = queryStr.get("out_trade_no").get.head
        val trade_status = queryStr.get("trade_status").get.head
        // 验证支付宝签名
        // 支付宝公钥
        val content = body_str + buyer_id_str + buyer_email_str + discount_str + gmt_create_str + gmt_payment_str +
          gmt_refund_str + is_total_fee_adjust_str + notify_id_str + notify_time_str + notify_type_str + out_trade_no_str +
          payment_type_str + price_str + quantity_str + refund_status_str + seller_id_str + seller_email_str + subject_str +
          total_fee_str + trade_no_str + trade_status_str + use_coupon_str

        if (OrderAPI.verifyAlipay(content, sign)) { // 验证通过
          // 根据支付宝的回调结果修改订单状态
          OrderAPI.updateOrderStatus(out_trade_no, trade_status)
          // 返回支付宝信息
          Results.Ok("success")
        } else {
          HanseResult.forbidden(HanseResult.RetCode.FORBIDDEN, errorMsg = Some("invaild request"))
        }
        Option(Results.Ok("success"))
      }
      Future {
        ret.get
      }
      // http://api.lvxingpai.com/notifyResult?discount=0.00&payment_type=1&subject= 测试&trade_no=2013082244524842&buyer_email=dlwdgl@gmail.com&gmt_create=2013-08-22 14:45:23&notify_type=trade_status_sync&quantity=1&out_trade_no=082215222612710&seller_id=2088501624816263&notify_time=2013-08-22 14:45:24&body=测试测试&trade_status=TRADE_SUCCESS&is_total_fee_adjust=N&total_fee=1.00&gmt_payment=2013-08-22 14:45:24&seller_email=xxx@alipay.com&price=1.00&buyer_id=2088602315385429&notify_id=64ce1b6ab92d00ede0ee56ade98fdf2f4c&use_coupon=N&sign_type=RSA&sign=1glihU9DPWee+UJ82u3+mw3Bdnr9u01at0M/xJnPsGuHh+JA5bk3zbWaoWhU6GmLab3dIM4JNdktTcEUI9/FBGhgfLO39BKX/eBCFQ3bXAmIZn4l26fiwoO613BptT44GTEtnPiQ6+tnLsGlVSrFZaLB9FVhrGfipH2SWJcnwYs=
    })
}
