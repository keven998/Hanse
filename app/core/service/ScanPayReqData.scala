package core.service

import scala.beans.BeanProperty

/**
 *
 * Created by topy on 2015/10/30.
 */
class ScanPayReqData {

  /**
   * 公众账号ID
   */
  @BeanProperty
  var appid: String = null

  /**
   * 商户号
   */
  @BeanProperty
  var mch_id: String = null

  /**
   * 设备号(可选)
   */
  @BeanProperty
  var device_info: String = null

  /**
   * 随机数
   */
  @BeanProperty
  var nonce_str: String = null

  /**
   * 签名
   */
  @BeanProperty
  var sign: String = null

  /**
   * 商品描述
   */
  @BeanProperty
  var body: String = null

  /**
   * 附加数据(可选)
   */
  @BeanProperty
  var attach: String = null

  /**
   * 商户订单号
   */
  @BeanProperty
  var out_trade_no: String = null

  /**
   * 总金额
   */
  @BeanProperty
  var total_fee: Int = 0

  /**
   * 终端IP
   */
  @BeanProperty
  var spbill_create_ip: String = null

  /**
   * 交易起始时间(可选)
   */
  @BeanProperty
  var time_start: String = null

  /**
   * 交易结束时间(可选)
   */
  @BeanProperty
  var time_expire: String = null

  /**
   * 商品标记(可选)
   */
  @BeanProperty
  var goods_tag: String = null

  /**
   *
   */
  @BeanProperty
  var auth_code: String = null

  /**
   * 付款银行
   */
  @BeanProperty
  var bank_type: String = null

  /**
   * 微信订单号
   *
   */
  @BeanProperty
  var transaction_id: String = null

}

object ScanPayReqData {

}
