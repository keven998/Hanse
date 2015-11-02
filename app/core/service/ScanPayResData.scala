package core.service

/**
 * Created by topy on 2015/10/30.
 */
class ScanPayResData {

  //协议层
  var return_code: String = null
  var return_msg: String = null

  //协议返回的具体数据（以下字段在return_code 为SUCCESS 的时候有返回）
  var appid: String = null
  var mch_id: String = null
  var nonce_str: String = null
  var sign: String = null
  var result_code: String = null
  var err_code: String = null
  var err_code_des: String = null

  var device_info: String = null

  //业务返回的具体数据（以下字段在return_code 和result_code 都为SUCCESS 的时候有返回）
  var openid: String = null
  var is_subscribe: String = null
  var trade_type: String = null
  var bank_type: String = null
  var total_fee: String = null
  var coupon_fee: String = null
  var fee_type: String = null
  var transaction_id: String = null
  var out_trade_no: String = null
  var attach: String = null
  var time_end: String = null

}

object ScanPayResData {

}
