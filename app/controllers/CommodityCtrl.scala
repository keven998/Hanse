package controllers

import javax.inject.{ Inject, Named }

import com.fasterxml.jackson.databind.node.ObjectNode
import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.yunkai.UserInfoProp
import com.lvxingpai.yunkai.Userservice.{ FinagledClient => YunkaiClient }
import controllers.security.AuthenticatedAction
import core.api.{ CommodityAPI, MiscAPI, SellerAPI }
import core.formatter.marketplace.product.{ CommodityCommentFormatter, CommodityCategoryFormatter, CommodityFormatter, SimpleCommodityFormatter }
import core.misc.HanseResult
import core.misc.Implicits.TwitterConverter._
import play.api.mvc.Controller
import play.api.{ Configuration, Play }

import scala.collection.JavaConverters._
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by topy on 2015/11/26.
 */
class CommodityCtrl @Inject() (@Named("default") configuration: Configuration, datastore: MorphiaMap) extends Controller {

  implicit val ds = datastore.map.get("k2").get

  def getCommodityDetail(commodityId: Long, version: Option[Long]) = AuthenticatedAction.async2(
    request => {
      val userOpt = request.headers.get("X-Lvxingpai-Id") map (_.toLong)
      for {
        commodity <- CommodityAPI.getCommodityById(commodityId, version)
        seller <- SellerAPI.getSeller(commodity)
        fas <- userOpt map (userId => MiscAPI.getFavorite(userId, "commodity")) getOrElse Future.successful(None)
      } yield {
        if (commodity.nonEmpty) {
          if (seller.nonEmpty) commodity.get.seller = seller.get
          val node = CommodityFormatter.instance.formatJsonNode(commodity.get).asInstanceOf[ObjectNode]
          node.put("shareUrl", "http://h5.taozilvxing.com/xq/detail.php?pid=" + commodity.get.commodityId)
          node.put("isFavorite", fas exists {
            _.commodities contains commodity.get.id
          })
          HanseResult(data = Some(node))
        } else
          HanseResult.notFound(Some(s"Commodity not found. sellId is $commodityId"))
      }
    }
  )

  def getCommodities(sellerId: Option[Long], locId: Option[String], category: Option[String],
    sortBy: String, sort: String,
    start: Int, count: Int) = AuthenticatedAction.async2(
    request => {
      for {
        commodities <- CommodityAPI.getCommodities(sellerId, locId, category, sortBy, sort, start, count)
      } yield {
        val node = SimpleCommodityFormatter.instance.formatJsonNode(commodities)
        HanseResult(data = Some(node))
      }
    }
  )

  def getCommodityCategory(locId: String) = AuthenticatedAction.async2(
    request => {
      for {
        commodities <- CommodityAPI.getCommodityCategories(locId)
      } yield {
        val cas = if (commodities == null) Seq() else commodities.flatMap(_.category.asScala.toSeq).distinct
        val node = CommodityCategoryFormatter.instance.formatJsonNode(cas)
        HanseResult(data = Some(node))
      }
    }
  )

  def addComment(commodityId: Long) = AuthenticatedAction.async2(
    request => {
      val yunkai = Play.application.injector instanceOf classOf[YunkaiClient]
      (for {
        body <- request.body.wrapped.asJson
        userId <- request.headers.get("X-Lvxingpai-Id") map (_.toLong)
        contents <- (body \ "contents").asOpt[String]
        rating <- Option((body \ "rating").asOpt[Double])
      } yield {
        import com.lvxingpai.yunkai.UserInfo

        val future: Future[UserInfo] = yunkai.getUserById(userId, Some(Seq(UserInfoProp.UserId, UserInfoProp.NickName, UserInfoProp.Avatar)))
        for {
          userInfo <- future
          commodities <- CommodityAPI.addComments(commodityId, userInfo, contents, rating, None)
        } yield {
          HanseResult.ok()
        }
      }) getOrElse Future {
        HanseResult.unprocessable()
      }
    }
  )

  def getComment(commodityId: Long) = AuthenticatedAction.async2(
    request => {
      for {
        commodities <- CommodityAPI.getComments(commodityId)
      } yield {
        if (commodities.nonEmpty) {
          val node = CommodityCommentFormatter.instance.formatJsonNode(commodities.get)
          HanseResult(data = Some(node))
        } else
          HanseResult.notFound()
      }
    }
  )

}
