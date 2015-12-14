package core.model.trade.order

import scala.beans.BeanProperty

/**
 * Created by topy on 2015/10/30.
 */
class WechatPrepay extends Prepay {

  /**
   * 返回状态码
   */
  @BeanProperty
  var returnCode: String = null

  /**
   * 返回状信息
   */
  @BeanProperty
  var returnMsg: String = null

  /**
   * 业务结果
   */
  @BeanProperty
  var resultCode: String = null

  /**
   * 错误代码
   */
  @BeanProperty
  var errCode: String = null

  /**
   * 错误代码描述
   */
  @BeanProperty
  var errCodeDesc: String = null

  /**
   * 用户标识
   */
  @BeanProperty
  var openId: String = null

  /**
   * 付款银行
   */
  @BeanProperty
  var bankType: String = null

  /**
   * 总金额
   */
  @BeanProperty
  var totalFee: Int = 0

  /**
   * 货币种类
   */
  @BeanProperty
  var feeType: String = null

  /**
   * 现金支付金额
   */
  @BeanProperty
  var cashFee: Int = 0

  /**
   * 现金支付货币类型
   */
  @BeanProperty
  var cashFeeType: String = null

}

object WechatPrepay {

  val FD_RETURN_MSG = "return_msg"

  val FD_RESULT_CODE = "result_code"

  val FD_ERR_CODE = "err_code"

  val FD_ERR_CODE_DES = "err_code_des"

  val FD_BANK_TYPE = "bank_type"

  val FD_TOTAL_FEE = "total_fee"

  val FD_FEE_TYPE = "fee_type"

  val FD_CASH_FEE = "cash_fee"

  val FD_CASH_FEE_TYPE = "cash_fee_type"

  val FD_OUT_TRADE_NO = "out_trade_no"

  val FD_TRANSACTION_ID = "transaction_id"

  val FD_TIME_END = "time_end"

  val FD_NONCE_STR = "nonce_str"

  val FD_SIGN = "sign"

  val FD_OPENID = "openid"

  val FD_TRADE_TYPE = "trade_type"

  val FD_RETURN_CODE = "return_code"

  val FD_APPID = "appid"

  val FD_ATTACH = "attach"

  val FD_BODY = "body"

  val FD_SPBILL_CREATE_IP = "spbill_create_ip"

  val VA_SUCCESS = "SUCCESS"

  val VA_FAIL = "FAIL"

  val PREPAY_ID = "prepay_id"

  val MCH_ID = "mch_id"

}
