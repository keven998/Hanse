package core.api

import core.db.MorphiaFactory
import core.misc.Global
import core.model.trade.order.{ OrderStatus, Prepay, PaymentVendor, Order }
import core.sign.RSA
import play.api.libs.ws.WS
import play.api.Play.current

import scala.concurrent.Future
import org.bson.types.ObjectId
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
 * Created by topy on 2015/10/22.
 */
object OrderAPI {

  val ds = MorphiaFactory.datastore
  // 服务接口名称， 固定值
  val aliService = "mobile.securitypay.pay"

  // 签约合作者身份ID
  val aliPartner = Global.conf.getString("alipayAPI.partner").get

  // 签约卖家支付宝账号
  val aliSeller = Global.conf.getString("alipayAPI.seller").get

  // 异步回调路径
  val aliNotifyUrl = Global.conf.getString("alipayAPI.notifyUrl").get

  // 支付类型， 固定值
  val aliPaymentType = "1"

  // 设置未付款交易的超时时间
  val aliItBPay = "1440m"

  // 支付完，跳转到此Url
  val returnUrl = "returnUrl"

  // 签名
  val sign = "sign"

  /**
   * 根据订单id查询订单信息
   * @param orderId 订单id
   * @return 订单信息
   */
  def getOrder(orderId: ObjectId): Future[Order] = {
    Future {
      ds.find(classOf[Order], Order.FD_ID, orderId).get
    }
  }

  /**
   * 创建订单
   * @param cmyId 商品Id
   * @param qty 商品数量
   * @return
   */
  def addOrder(cmyId: String, qty: Int): Future[Order] = {
    val futureCmy = CommodityAPI.getCommodityById(cmyId)
    for {
      cmy <- futureCmy
    } yield {
      val order = Order(cmy, qty)
      ds.save[Order](order)
      order
    }
  }

  /**
   * 根据订单信息生成支付宝所需信息的字符串
   * @param orderInfo 订单信息
   * @return 支付宝支付所需信息
   */
  def getAlipayPrePayStr(orderInfo: Order): String = {

    // 签约合作者身份ID
    val partnerStr = "partner=" + "\"" + aliPartner + "\""

    // 签约卖家支付宝账号
    val sellerStr = "&seller_id=" + "\"" + aliSeller + "\""

    // 商户网站唯一订单号
    val orderIdStr = "&out_trade_no=" + "\"" + orderInfo.id + "\""

    // 商品名称
    val cmyNameStr = "&subject=" + "\"" + orderInfo.commodity.title + "\""

    // 商品详情
    val cmyDetailStr = "&body=" + "\"" + orderInfo.commodity.detail + "\""

    // 商品金额
    val totalFeeStr = "&total_fee=" + "\"" + orderInfo.totalPrice + "\""

    // 服务器异步通知页面路径
    val notifyUrlStr = "&notify_url=" + "\"" + aliNotifyUrl + "\""

    // 服务接口名称， 固定值
    val serviceStr = "&service=\"" + aliService + "\""

    // 支付类型， 固定值
    val paymentTypeStr = "&payment_type=\"" + aliPaymentType + "\""

    // 参数编码， 固定值
    val inputCharset = "&_input_charset=\"utf-8\""

    // 设置未付款交易的超时时间
    // 默认30分钟，一旦超时，该笔交易就会自动被关闭。
    // 取值范围：1m～15d。
    // m-分钟，h-小时，d-天，1c-当天（无论交易何时创建，都在0点关闭）。
    // 该参数数值不接受小数点，如1.5h，可转换为90m。
    val itBPayStr = "&it_b_pay=\"" + aliItBPay + "\""

    // extern_token为经过快登授权获取到的alipay_open_id,带上此参数用户将使用授权的账户进行支付
    // orderInfo += "&extern_token=" + "\"" + extern_token + "\"";

    // 调用银行卡支付，需配置此参数，参与签名， 固定值 （需要签约《无线银行卡快捷支付》才能使用）
    // orderInfo += "&paymethod=\"expressGateway\"";

    val orderInfoStr = partnerStr + sellerStr + orderIdStr + cmyNameStr + cmyDetailStr + totalFeeStr + notifyUrlStr + serviceStr +
      paymentTypeStr + inputCharset + itBPayStr

    // 签名
    val signStr = "&sign=\"" + RSA.sign(orderInfoStr, privateKey(), "utf-8") + "\""

    // 加密算法
    val signTypeStr = "&sign_type=\"RSA\""

    val result = orderInfoStr + signStr + signTypeStr

    result
  }

  /**
   * 根据订单信息生成微信所需信息的字符串
   * @param orderInfo 订单信息
   * @return 微信支付所需信息
   */
  def getWeixinPrePayStr(orderInfo: Order): String = {
    ""
  }

  /**
   * 根据订单信息生成银联所需信息的字符串
   * @param orderInfo 订单信息
   * @return 银联支付所需信息
   */
  def getUnionPrePayStr(orderInfo: Order): String = {
    ""
  }

  /**
   * 取得私钥字符串
   * @return 私钥字符串
   */
  def privateKey(): String = {
    scala.io.Source.fromFile("conf/rsa_private_key.pem").getLines().mkString("")
  }

  /**
   * 更新预支付信息
   * @param paymentVendor 预支付商家
   * @param amount 金额
   * @param order 订单
   */
  def updatePayment(paymentVendor: String, amount: Float, order: Order): Unit = {
    val prepay = new Prepay()
    prepay.vendor = paymentVendor
    prepay.amount = amount
    prepay.timestamp = System.currentTimeMillis()

    order.payments.put(paymentVendor, prepay)
    val query = ds.createQuery(classOf[Order]).field(Order.FD_ID).equal(order.id)
    val updateOps = ds.createUpdateOperations(classOf[Order]).set(Order.FD_PAYMENTS, order.payments)
    ds.updateFirst(query, updateOps)
  }

  /**
   * 预支付
   * @param payMerchant 支付商
   * @param orderId 订单ID
   * @return 预支付信息
   */
  def prePay(payMerchant: String, orderId: String): Future[String] = {
    // 取得订单信息
    val futureOrderInfo = getOrder(new ObjectId(orderId))

    // 根据不同的支付商家, 返回不同的串
    payMerchant match {
      case PaymentVendor.Alipay => futureOrderInfo map (orderInfo => {
        updatePayment(PaymentVendor.Alipay, orderInfo.totalPrice, orderInfo)
        getAlipayPrePayStr(orderInfo)
      })
      case PaymentVendor.Wechat => futureOrderInfo map (orderInfo => {
        updatePayment(PaymentVendor.Wechat, orderInfo.totalPrice, orderInfo)
        getWeixinPrePayStr(orderInfo)
      })
      case PaymentVendor.UnionPay => futureOrderInfo map (orderInfo => {
        updatePayment(PaymentVendor.UnionPay, orderInfo.totalPrice, orderInfo)
        getUnionPrePayStr(orderInfo)
      })
      case _ => throw new IllegalArgumentException
    }
  }

  /**
   * 根据订单号查询支付宝的订单支付状态
   * @param orderId 订单号
   * @return
   */
  def getAlipayOrderStatus(orderId: String): String = {
    // 请求参数
    //    val data = {
    //      "method"="alipay.trade.query",
    //      "app_id"="2014072300007148",
    //      "charset"="utf-8",
    //      "sign_type"="RSA",
    //      "sign"="MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBAL4HgSt9hfOpv9MSwgUvfpgOH7WkC7WrTr84m29b0VfnZtK+9jv/YPXYr+22DrxfWJkdPiXJvSjifxPudlqjX21l6/8k79i/4HRCz8DBcdw5jqROfpoq0l3vYfPVqJGwSqaPhtM1Bb4hAD2yLlN2ukfCbshSntUEFd4ozocLolW/AgMBAAECgYBVHTNj8WMQElYTCnHQtMc1AA5/4yxDgKlSyN4F8NBBWSoa9uF/WhFpzFZwWH0dLm+WlRyC/Gs3ZsuYd9SXIFna9mv49+cEfObSzJhvW5DXOVCi+c4Ap3cTsXZiAj8DMsoCb9OjRHNl/BqzN0kl0Wm1diZvXl9YgSRqbzpXzoj4oQJBAOYLSk87XFYodvwr4aL3KFjZZZhHj1Jpp/q61SNgB03aXqZu9m+hk1X4mTGn4rhA7Cl2ZuL+OoxxnJFDw0cbMRcCQQDTeGgx0VUC+O3zAtzMmocjE7WuesRC3IjhU30of4GGjQzIXvKOQCCuUF2DHvIkrB/k2E75n8+TI9matbLS11mZAkEAtSek7/oF/89Dy9dei2/o9PbVu3J22eZcIuVoHMBtYBCbwqLVLBloJiZrtR/JOWHe19Pmt9COGLULH5XmPKOcJwJAUZnP0xFs1XXLFA/Rtd4XMXDklYxn+UjyRMibrintiEcbXKJOxJd4ROtb+kHRvFbzA7J4XxjM14Fo8asVcwiIWQJAVco+9qQzZ7JZzFzk0KTWhQlfbcRByLX25XIPbIes2lmY2uM895yrY/8kbGx2JgD/VGITWwth+uuutXUQ9K6HIw==",
    //      "timestamp"="2014-07-24 03:07:50",
    //      "biz_content"={
    //        "out_trade_no": "U12560-151015-9201"
    //      }
    //    }

    // 支付成功TRADE_SUCCESS 或者 TRADE_FINISHED
    //    {
    //      "alipay_trade_query_response": {
    //        "code": "10000",
    //        "msg": "处理成功",
    //        "trade_no": "2013112011001004330000121536",
    //        "out_trade_no": "6823789339978248",
    //        "trade_status": "TRADE_SUCCESS",
    //        "buyer_user_id": "2088102122524333",
    //        "buyer_logon_id": "159****5620",
    //        "total_fee": "88.88",
    //        "receipt_amount": "8.88",
    //        "send_pay_date": "2014-11-27 15:45:57",
    //        "store_id":"NJ_S_001",
    //        "store_name":"证大五道口店",
    //        "terminal_id":"NJ_T_001",
    //        "fund_bill_list": [
    //      {
    //        "fund_channel": "ALIPAYACCOUNT",
    //        "amount": "80.00"
    //      },
    //      {
    //        "fund_channel": "DISCOUNT",
    //        "amount": "8.88"
    //      }
    //        ]
    //      },
    //      "sign": "jfAz0Yi0OUvAPqYTzA0DLysx0ri++yf7o/lkHOHaG1Zy2fHBf3j4WM+sJWHZUuyInt6V+wn+6IP9AmwRTKi+GGdWjPrsfBjXqR7H5aBnLhMsAltV7v4cYjhuguAqh4WkaJO6v6CfdybDpzHlxE6Thoucnad+OsjdCXkNd1g3UuU="
    //    }

    // 提交支付，但是未支付

    //    {
    //      "alipay_trade_query_response": {
    //        "code": "10000",
    //        "msg": "处理成功",
    //        "trade_no": "2013112011001004330000121536",
    //        "out_trade_no": "6823789339978248",
    //        "trade_status": "WAIT_BUYER_PAY",
    //        "buyer_user_id": "2088102122524333",
    //        "buyer_logon_id": "159****5620",
    //        "total_amount": "88.88",
    //        "store_id":"NJ_S_001",
    //        "store_name":"证大五道口店",
    //        "terminal_id":"NJ_T_001",
    //      },
    //      "sign": "jfAz0Yi0OUvAPqYTzA0DLysx0ri++yf7o/lkHOHaG1Zy2fHBf3j4WM+sJWHZUuyInt6V+wn+6IP9AmwRTKi+GGdWjPrsfBjXqR7H5aBnLhMsAltV7v4cYjhuguAqh4WkaJO6v6CfdybDpzHlxE6Thoucnad+OsjdCXkNd1g3UuU="
    //    }

    // 处理失败
    //    {
    //      "alipay_trade_query_response": {
    //        "code": "40004",
    //        "msg": "处理失败",
    //        "sub_code": "ACQ.TRADE_NOT_EXIST",
    //        "sub_desc": "交易不存在"
    //      },
    //      "sign": "jfAz0Yi0OUvAPqYTzA0DLysx0ri++yf7o/lkHOHaG1Zy2fHBf3j4WM+sJWHZUuyInt6V+wn+6IP9AmwRTKi+GGdWjPrsfBjXqR7H5aBnLhMsAltV7v4cYjhuguAqh4WkaJO6v6CfdybDpzHlxE6Thoucnad+OsjdCXkNd1g3UuU="
    //    }

    ""
  }

  /**
   * 更新订单状态
   * @param orderId 订单号
   * @param status 订单状态
   */
  def updateOrderStatus(orderId: String, status: String): Unit = {
    val query = ds.createQuery(classOf[Order]).field(Order.FD_ID).equal(new ObjectId(orderId))
    val updateOps = ds.createUpdateOperations(classOf[Order]).set(Order.FD_STATUS, status)
    ds.update(query, updateOps)
  }

  def aliOrderStatus2OrderStatus(aliTradeStatus: String): String = {
    aliTradeStatus match {
      case "TRADE_SUCCESS" | "TRADE_FINISHED" => OrderStatus.Finished
      case "WAIT_BUYER_PAY" => OrderStatus.Pending
      case "TRADE_CLOSED" => OrderStatus.Close
    }
  }

  /**
   * 根据订单号查询订单支付状态, 如果支付成功, 直接返回, 其他状态
   * @param orderId 订单号
   * @return 订单状态
   */
  def getOrderStatus(orderId: String): Future[String] = {
    // 根据订单号查询订单支付信息
    Future {
      val order = ds.createQuery(classOf[Order]).field(Order.FD_ID).equal(new ObjectId(orderId)).get
      if (order.status.equals(OrderStatus.Finished)) order.status
      else {
        // 订单未完成, 去支付宝查询订单状态, 核对订单状态
        val alipayStatus = aliOrderStatus2OrderStatus(getAlipayOrderStatus(orderId))
        if (!alipayStatus.equals(order.status)) {
          updateOrderStatus(orderId, alipayStatus)
          alipayStatus
        } else order.status
      }
    }
  }

  /**
   * 验证支付宝签名
   * @param content 签名所需的数据
   * @param sign 签名
   * @return 签名是否通过
   */
  def verifyAlipay(content: String, sign: String): Boolean = {
    val ali_public_key = Global.conf.getString("alipayAPI.aliPubKey").get
    val input_charset = "utf-8"
    RSA.verify(content, sign, ali_public_key, input_charset)
  }
}
