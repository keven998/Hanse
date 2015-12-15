package controllers

import javax.inject.{ Named, Inject, Singleton }

import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.model.marketplace.order.Order
import com.lvxingpai.model.marketplace.trade.PaymentVendor
import core.api.OrderAPI
import core.misc.HanseResult
import core.misc.Implicits._
import core.model.trade.order.WechatPrepay
import core.service.PaymentService
import play.api.Configuration
import play.api.mvc.{ Result, Action, Controller }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps
import scala.xml.NodeSeq

@Singleton
class PaymentCtrl @Inject() (@Named("default") configuration: Configuration, datastore: MorphiaMap) extends Controller {

  implicit lazy val ds = datastore.map.get("k2").get

  /**
   * 生成预支付对象
   *
   * @param orderId
   * @return
   */
  def createPayments(orderId: Long) = Action.async(
    request => {
      val ip = request.remoteAddress
      val ret = for {
        body <- request.body.asJson
        userId <- request.headers.get("UserId") map (_.toLong)
        //        ip <- (body \ "ip").asOpt[String].getOrElse("")
        tradeType <- (body \ "tradeType").asOpt[String] orElse Option("None")
        vendor <- (body \ "vendor").asOpt[String]
      } yield {
        if (vendor.equals(PaymentVendor.Wechat))
          getWechatPaymentResult(userId: Long, orderId: Long, tradeType: String, ip: String)
        else if (vendor.equals(PaymentVendor.Alipay))
          Future {
            HanseResult.ok()
          }
        else
          Future {
            HanseResult.ok()
          }
      }
      ret getOrElse Future {
        HanseResult.unprocessable()
      }
    }
  )

  /**
   * 取得微信支付的预支付对象
   *
   * @param userId
   * @param orderId
   * @param tradeType
   * @param ip
   * @return
   */
  def getWechatPaymentResult(userId: Long, orderId: Long, tradeType: String, ip: String): Future[Result] = {
    val ret = for {
      orderValue <- OrderAPI.getOrder(orderId, Seq("orderId", "commodity", "totalPrice"))
      wcResponse <- PaymentService.unifiedOrder(
        Map(
          WechatPrepay.FD_OUT_TRADE_NO -> orderId,
          WechatPrepay.FD_SPBILL_CREATE_IP -> ip,
          WechatPrepay.FD_BODY -> orderValue.commodity.title,
          WechatPrepay.FD_TRADE_TYPE -> tradeType,
          WechatPrepay.FD_TOTAL_FEE -> (orderValue.totalPrice * 100).toInt
        )
      )
      result <- OrderAPI.savePrepay(
        PaymentService.xml2Obj(new String(wcResponse.bodyAsBytes, "UTF8")),
        orderValue
      )
    } yield {
      val str = new String(wcResponse.bodyAsBytes, "UTF8")
      val ret = PaymentService.xml2OResult(str)
      HanseResult(data = Some(ret))
    }
    ret
  }

  val wechatCallBackOK =
    <xml>
      <return_code>
        <![CDATA[SUCCESS]]>
      </return_code>
      <return_msg>
        <![CDATA[OK]]>
      </return_msg>
    </xml>

  val wechatCallBackError =
    <xml>
      <return_code>
        <![CDATA[FAIL]]>
      </return_code>
      <return_msg>
        <![CDATA[FAIL]]>
      </return_msg>
    </xml>

  def wechatCallback() = Action.async(
    request => {
      val ret = for {
        body <- request.body.asXml
      } yield {
        val prePay = getCallbackBody(body)
        // 验证returnCode
        val flag = prePay.getReturnCode.equals(WechatPrepay.VA_SUCCESS) &&
          prePay.getResultCode.equals(WechatPrepay.VA_SUCCESS)
        if (flag) {
          val orderId = (body \ WechatPrepay.FD_OUT_TRADE_NO \*).toString().toLong
          OrderAPI.getOrder(orderId).map(order => {
            // 验证请求的签名等信息
            if (validationCallback(prePay, order)) {
              OrderAPI.savePrepay(Some(prePay), order)
              OrderAPI.updateOrderStatus(orderId, "paid")
              //              for {
              //                resultPre <- OrderAPI.savePrepay(Some(prePay), order)
              //                updateSta <- OrderAPI.updateOrderStatus(orderId, OrderStatus.Paid)
              //              } yield resultPre
              Ok(wechatCallBackOK)
            } else
              Ok(wechatCallBackError)
          })
        } else
          Future {
            Ok(wechatCallBackError)
          }
      }
      ret getOrElse Future {
        Ok(wechatCallBackError)
      }
    }

  )

  def getCallbackBody(body: NodeSeq) = {
    val payment = new WechatPrepay()
    val returnCode = (body \ WechatPrepay.FD_RETURN_CODE \*).toString()
    payment.setResult(returnCode)
    payment.setReturnCode(returnCode)
    payment.setReturnMsg((body \ WechatPrepay.FD_RETURN_MSG \*).toString())
    if (returnCode.equals(WechatPrepay.VA_FAIL)) {
      payment
    } else {
      payment.setResultCode((body \ WechatPrepay.FD_RESULT_CODE \*).toString())
      payment.setErrCode((body \ WechatPrepay.FD_ERR_CODE \*).toString())
      payment.setErrCode((body \ WechatPrepay.FD_ERR_CODE_DES \*).toString())
      payment.setNonceString((body \ WechatPrepay.FD_NONCE_STR \*).toString())
      payment.setSign((body \ WechatPrepay.FD_SIGN \*).toString())
      payment.setOpenId((body \ WechatPrepay.FD_OPENID \*).toString())
      payment.setTradeType((body \ WechatPrepay.FD_TRADE_TYPE \*).toString())
      payment.setBankType((body \ WechatPrepay.FD_BANK_TYPE \*).toString())
      payment.setTotalFee(body \ WechatPrepay.FD_TOTAL_FEE \*)
      payment.setFeeType((body \ WechatPrepay.FD_FEE_TYPE \*).toString())
      payment.setCashFee(body \ WechatPrepay.FD_CASH_FEE \*)
      payment.setCashFeeType((body \ WechatPrepay.FD_CASH_FEE_TYPE \*).toString())
      payment.setPrepayId((body \ WechatPrepay.FD_TRANSACTION_ID \*).toString())
      payment.setTimestamp((body \ "time_end" \*).toString().toLong)
      payment
    }
  }

  def validationCallback(wechatPrepay: WechatPrepay, order: Order): Boolean = {
    if (order.paymentInfo.isEmpty)
      false
    else {
      val dbPrepay = order.paymentInfo.get(PaymentVendor.Wechat)
      val ret = dbPrepay.sign.equals(wechatPrepay.sign) &&
        dbPrepay.nonceString.equals(wechatPrepay.nonceString) &&
        dbPrepay.prepayId.equals(wechatPrepay.prepayId)
      // dbPrepay.getVendor.equals(prepay.getVendor) &&
      // dbPrepay.getTradeType.equals(prepay.getTradeType)
      ret
    }

  }

  def getPaymentsStatus(orderId: Long) = Action.async(
    request => {
      val ret = for {
        orderValue <- OrderAPI.getOrder(orderId)
        wcResponse <- PaymentService.queryOrder(
          Map(WechatPrepay.FD_TRANSACTION_ID ->
            orderValue.paymentInfo.get(PaymentVendor.Wechat).prepayId)
        )
      } yield {
        val str = orderValue.status
        Ok(str)
      }
      ret
    }
  )

  def getPayments(orderId: Long, provider: String) = Action.async(
    request => {
      val ret = for {
        orderValue <- OrderAPI.getOrder(orderId)
        wcResponse <- PaymentService.queryOrder(
          Map(WechatPrepay.FD_TRANSACTION_ID ->
            orderValue.paymentInfo.get(PaymentVendor.Wechat).prepayId)
        )
      } yield {
        val str = orderValue.status
        Ok(str)
      }
      ret
    }
  )

}
