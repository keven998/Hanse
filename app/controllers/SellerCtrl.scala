package controllers

import javax.inject.{ Inject, Named, Singleton }

import com.fasterxml.jackson.databind.node.ObjectNode
import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.model.marketplace.order.Order
import com.lvxingpai.yunkai.UserInfoProp
import controllers.security.AuthenticatedAction
import core.api.{ BountyAPI, OrderAPI, SellerAPI }
import core.exception.ResourceNotFoundException
import core.formatter.geo.SimpleLocalityFormatter
import core.formatter.marketplace.order.{ ScheduleFormatter, SimpleBountyFormatter }
import core.formatter.marketplace.seller.SellerFormatter
import core.misc.Implicits.{ TempLocality, _ }
import core.misc.{ HanseResult, Utils }
import org.apache.commons.lang.StringUtils
import play.api.Configuration
import play.api.mvc.{ Action, Controller }

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
      val totalOrder = Seq(Order.Status.Committed, Order.Status.Reviewed, Order.Status.ToReview, Order.Status.Paid,
        Order.Status.RefundApplied, Order.Status.Finished) map (_.toString) mkString ","
      val suspendingOrder = Order.Status.Paid.toString + "," + Order.Status.RefundApplied.toString
      val ret = for {
        seller <- SellerAPI.getSeller(id)
        orders <- OrderAPI.getOrderList(userId = None, sellerId = Some(id), Some(totalOrder), 0, Int.MaxValue, Seq("totalPrice"))
        ordersCnt <- OrderAPI.getOrderCnt(userId = None, sellerId = Some(id), None)
        suspendingOrders <- OrderAPI.getOrderList(userId = None, sellerId = Some(id), Some(suspendingOrder), 0, Int.MaxValue, Seq("_id"))
        //user <- FinagleFactory.client.getUserById(sId, Some(fields), selfId)
      } yield {
        if (seller.nonEmpty) {
          val ret = seller map (r => {
            val node = SellerFormatter.instance.formatJsonNode(r).asInstanceOf[ObjectNode]
            node.put("totalSales", Utils.getActualPrice((orders map (_.totalPrice)).sum))
            node.put("totalOrderCnt", ordersCnt)
            node.put("pendingOrderCnt", suspendingOrders.size)

            //node.put("detailUrl", "http://h5.lvxingpai.com/shop/myshop.php?sid"+id)
            //node.put("shareUrl", "http://h5.lvxingpai.com/shop/myshop.php?sid="+id)
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
  def becomeSeller() = Action.async(
    request => {
      val ret = for {
        body <- request.body.asJson
        memo <- (body \ "memo").asOpt[String] orElse Some(StringUtils.EMPTY)
        email <- (body \ "email").asOpt[String] orElse Some(StringUtils.EMPTY)
        tel <- (body \ "tel").asOpt[String] orElse Some(StringUtils.EMPTY)
        name <- (body \ "name").asOpt[String]
        province <- (body \ "province").asOpt[String] orElse Some(StringUtils.EMPTY)
        city <- (body \ "city").asOpt[String] orElse Some(StringUtils.EMPTY)
        travel <- (body \ "travel").asOpt[String] orElse Some(StringUtils.EMPTY)
        license <- (body \ "license").asOpt[String] orElse Some(StringUtils.EMPTY)
      } yield {
        for {
          _ <- SellerAPI.addApplySeller(name, tel, province, city, travel, license, email, memo)
        } yield {
          HanseResult.ok()
        }
      }
      ret.getOrElse(Future {
        HanseResult.unprocessable()
      })
    }
  )

  /**
   * 添加商家的订阅城市
   *
   * @return 返回订单信息
   */
  def addSubLocalities() = AuthenticatedAction.async2(
    request => {
      val userId = (request.headers get "X-Lvxingpai-Id" getOrElse "").toLong
      val ret = for {
        body <- request.body.wrapped.asJson
        localities <- (body \ "localities").asOpt[Array[TempLocality]]
      } yield {
        for {
          bounty <- SellerAPI.addSubLocalities(userId, localities.toSeq)
        } yield HanseResult.ok()
      } recover {
        case e: ResourceNotFoundException => HanseResult.unprocessable(errorMsg = Some(e.getMessage))
      }
      ret getOrElse Future {
        HanseResult.unprocessable()
      }
    }
  )

  /**
   * 取得商家的订阅城市列表
   *
   * @return
   */
  def getSubLocalities(sellerId: Long) = AuthenticatedAction.async2(
    request => {
      val userId = (request.headers get "X-Lvxingpai-Id" getOrElse "").toLong
      val ret = for {
        localities <- SellerAPI.getSubLocalities(sellerId)
      } yield HanseResult(data = Some(SimpleLocalityFormatter.instance.formatJsonNode(localities getOrElse Seq())))
      ret
    }
  )

  /**
   * 取得商家提交过的方案
   *
   * @param sellerId
   * @param sortBy
   * @param sort
   * @param start
   * @param count
   * @return
   */
  def getSchedules(sellerId: Long, sortBy: String, sort: String, start: Int, count: Int) = AuthenticatedAction.async2(
    request => {
      val userId = (request.headers get "X-Lvxingpai-Id" getOrElse "").toLong
      val ret = for {
        schedule <- BountyAPI.getScheduleBySellerId(sellerId, sortBy, sort, start, count)
      } yield HanseResult(data = Some(ScheduleFormatter.instance.formatJsonNode(schedule)))
      ret
    }
  )

  /**
   * 商家接过的单
   *
   * @param sellerId
   * @param sortBy
   * @param sort
   * @param start
   * @param count
   * @return
   */
  def getBounties(sellerId: Long, sortBy: String, sort: String, start: Int, count: Int) = AuthenticatedAction.async2(
    request => {
      val userId = (request.headers get "X-Lvxingpai-Id" getOrElse "").toLong
      val ret = for {
        schedule <- BountyAPI.getBountyBySellerId(sellerId, sortBy, sort, start, count)
      } yield HanseResult(data = Some(SimpleBountyFormatter.instance.formatJsonNode(schedule)))
      ret
    }
  )

}
