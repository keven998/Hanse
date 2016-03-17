package controllers

import javax.inject.{ Inject, Named, Singleton }

import com.fasterxml.jackson.databind.node.ObjectNode
import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.model.marketplace.order.Order
import com.lvxingpai.yunkai.UserInfoProp
import controllers.security.AuthenticatedAction
import core.api.{ OrderAPI, SellerAPI }
import core.formatter.marketplace.seller.SellerFormatter
import core.misc.{ Utils, HanseResult }
import core.misc.Implicits.PhoneNumberTemp
import org.apache.commons.lang.StringUtils
import play.api.Configuration
import play.api.mvc.Controller

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by topy on 2015/11/3.
 */
@Singleton
class SellerCtrl @Inject() (@Named("default") configuration: Configuration, datastore: MorphiaMap) extends Controller {

  import UserInfoProp._

  implicit lazy val ds = datastore.map.get("k2").get

  val fields = Seq(UserId, NickName, Avatar, Gender, Signature, Residence, Birthday)

  def getSeller(id: Long) = AuthenticatedAction.async2(

    request => {
      val totalOrder = Order.Status.Committed + "," + Order.Status.Reviewed + "," + Order.Status.ToReview
      val suspendingOrder = Order.Status.Paid.toString + "," + Order.Status.RefundApplied.toString
      val ret = for {
        seller <- SellerAPI.getSeller(id)
        orders <- OrderAPI.getOrderList(userId = None, sellerId = Some(id), Some(totalOrder), 0, Int.MaxValue, Seq("totalPrice"))
        suspendingOrders <- OrderAPI.getOrderList(userId = None, sellerId = Some(id), Some(suspendingOrder), 0, Int.MaxValue, Seq("_id"))
        //user <- FinagleFactory.client.getUserById(sId, Some(fields), selfId)
      } yield {
        if (seller.nonEmpty) {
          val ret = seller map (r => {
            val node = SellerFormatter.instance.formatJsonNode(r).asInstanceOf[ObjectNode]
            node.put("totalSales", Utils.getActualPrice((orders map (_.totalPrice)).sum))
            node.put("totalOrderCnt", orders.size)
            node.put("pendingOrderCnt", suspendingOrders.size)
          })
          HanseResult(data = ret)
        } else
          HanseResult.notFound(Some(s"Seller not found. sellId is $id"))
      }
      ret
    }
  )

  /**
   * 申请成为商家
   * @return
   */
  def becomeSeller() = AuthenticatedAction.async2(
    request => {
      val ret = for {
        body <- request.body.wrapped.asJson
        userId <- (body \ "userId").asOpt[Long]
        memo <- (body \ "memo").asOpt[String] orElse Some(StringUtils.EMPTY)
        email <- (body \ "email").asOpt[String] orElse Some(StringUtils.EMPTY)
        tel <- (body \ "tel").asOpt[PhoneNumberTemp] // TODO 弃用PhoneNumberTemp之类的辅助类
      } yield {
        // TODO 用户申请成为商家的时候,可以发送相应的邮件给BD部门
        Future(HanseResult.ok())
      }
      ret.getOrElse(Future {
        HanseResult.unprocessable()
      })
    }
  )

}
