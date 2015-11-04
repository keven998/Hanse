package controllers

import javax.inject.Singleton

import core.api.OrderAPI
import core.misc.HanseResult
import core.misc.Implicits._
import core.model.trade.order._
import core.service.PaymentService
import org.bson.types.ObjectId
import org.mongodb.morphia.Datastore
import play.api.mvc.{ Action, Controller }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.xml.NodeSeq

@Singleton
class PaymentCtrl extends Controller {

  def createPayments(orderId: String) = Action.async(
    request => {
      val ret = for {
        body <- request.body.asJson
        userId = request.headers.get("UserId") map (_.toLong)
        name = (body \ "name").asOpt[String].getOrElse("WechatPayment")
        ip = (body \ "ip").asOpt[String].getOrElse("")
        tradeType = (body \ "tradeType").asOpt[String].getOrElse("None")
        vendor = (body \ "vendor").asOpt[String].getOrElse("")
      } yield {
        for {
          orderValue <- OrderAPI.getOrder(new ObjectId(orderId))
          wcResponse <- PaymentService.unifiedOrder(
            Map(WechatPrepay.FD_OUT_TRADE_NO -> orderId,
              WechatPrepay.FD_SPBILL_CREATE_IP -> ip,
              WechatPrepay.FD_BODY -> name,
              WechatPrepay.FD_TRADE_TYPE -> tradeType,
              WechatPrepay.FD_TOTAL_FEE -> (orderValue.getTotalPrice * 100).toInt))
          result <- OrderAPI.savePrepay(
            PaymentService.xml2Obj(new String(wcResponse.bodyAsBytes, "UTF8")),
            orderValue)
        } yield {
          val str = new String(wcResponse.bodyAsBytes, "UTF8")
          Ok(str)
        }
      }
      ret getOrElse Future {
        HanseResult.unprocessable()
      }
    }
  )

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
          val orderId = (body \ WechatPrepay.FD_OUT_TRADE_NO \*)
          OrderAPI.getOrder(new ObjectId(orderId)).map(order => {
            // 验证请求的签名等信息
            if (validationCallback(prePay, order)) {
              OrderAPI.savePrepay(Some(prePay), order)
              OrderAPI.updateOrderStatus(orderId, OrderStatus.Paid)
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
        HanseResult.unprocessable()
      }
    }

  )

  def getCallbackBody(body: NodeSeq) = {
    val payment = new WechatPrepay()
    val returnCode = (body \ WechatPrepay.FD_RETURN_CODE \*)
    payment.setResult(returnCode)
    payment.setReturnCode(returnCode)
    payment.setReturnMsg((body \ WechatPrepay.FD_RETURN_MSG \*))
    if (returnCode.equals(WechatPrepay.VA_FAIL)) {
      payment
    } else {
      payment.setResultCode((body \ WechatPrepay.FD_RESULT_CODE \*))
      payment.setErrCode((body \ WechatPrepay.FD_ERR_CODE \*))
      payment.setErrCode((body \ WechatPrepay.FD_ERR_CODE_DES \*))
      payment.setNonceString((body \ WechatPrepay.FD_NONCE_STR \*))
      payment.setSign((body \ WechatPrepay.FD_SIGN \*))
      payment.setOpenId((body \ WechatPrepay.FD_OPENID \*))
      payment.setTradeType((body \ WechatPrepay.FD_TRADE_TYPE \*))
      payment.setBankType((body \ WechatPrepay.FD_BANK_TYPE \*))
      payment.setTotalFee((body \ WechatPrepay.FD_TOTAL_FEE \*))
      payment.setFeeType((body \ WechatPrepay.FD_FEE_TYPE \*))
      payment.setCashFee((body \ WechatPrepay.FD_CASH_FEE \*))
      payment.setCashFeeType((body \ WechatPrepay.FD_CASH_FEE_TYPE \*))
      payment.setPrepayId((body \ WechatPrepay.FD_TRANSACTION_ID \*))
      payment.setTimestamp((body \ "time_end" \*).toString().toLong)
      payment
    }
  }

  def validationCallback(prepay: Prepay, order: Order) = {
    if (order.getPayments isEmpty)
      false
    else {
      val dbPrepay = order.getPayments.get(PaymentVendor.Wechat)
      val ret = dbPrepay.getSign.equals(prepay.getSign) &&
        dbPrepay.getNonceString.equals(prepay.getNonceString) &&
        dbPrepay.getPrepayId.equals(prepay.getPrepayId)
      // dbPrepay.getVendor.equals(prepay.getVendor) &&
      // dbPrepay.getTradeType.equals(prepay.getTradeType)
      ret
    }
  }

  def getPaymentsStatus(orderId: String) = Action.async(
    request => {
      val ret = for {
        orderValue <- OrderAPI.getOrder(new ObjectId(orderId))
        wcResponse <- PaymentService.queryOrder(
          Map(WechatPrepay.FD_TRANSACTION_ID ->
            orderValue.getPayments.get(PaymentVendor.Wechat).getPrepayId))
      } yield {
        val str = orderValue.getStatus
        Ok(str)
      }
      ret
    }
  )

  def getPayments(orderId: String) = Action.async(
    request => {
      val ret = for {
        orderValue <- OrderAPI.getOrder(new ObjectId(orderId))
        wcResponse <- PaymentService.queryOrder(
          Map(WechatPrepay.FD_TRANSACTION_ID ->
            orderValue.getPayments.get(PaymentVendor.Wechat).getPrepayId))
      } yield {
        val str = orderValue.getStatus
        Ok(str)
      }
      ret
    }
  )

}
