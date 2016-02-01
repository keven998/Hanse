package controllers

import javax.inject.{ Inject, Named }

import com.fasterxml.jackson.databind.node.ObjectNode
import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.yunkai.Userservice.{ FinagledClient => YunkaiClient }
import controllers.security.AuthenticatedAction
import core.api.{ CommodityAPI, MiscAPI, SellerAPI }
import core.exception.OrderStatusException
import core.formatter.marketplace.product.{ CommodityCategoryFormatter, CommodityCommentFormatter, CommodityFormatter, SimpleCommodityFormatter }
import core.misc.HanseResult
import core.misc.Implicits._
import play.api.Configuration
import play.api.mvc.Controller

import scala.collection.JavaConverters._
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
        commodities <- CommodityAPI.getComments(commodityId, 0, 1)
      } yield {
        if (commodity.nonEmpty) {
          if (seller.nonEmpty) commodity.get.seller = seller.get
          val node = CommodityFormatter.instance.formatJsonNode(commodity.get).asInstanceOf[ObjectNode]
          node.put("shareUrl", "http://h5.taozilvxing.com/xq/detail.php?pid=" + commodity.get.commodityId)
          node.put("comments", CommodityCommentFormatter.instance.formatJsonNode(commodities))
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

  /**
   * 针对商品发表评论
   *
   * @param commodityId
   * @return
   */
  def addComment(commodityId: Long) = AuthenticatedAction.async2(
    request => {
      (for {
        body <- request.body.wrapped.asJson
        contents <- (body \ "contents").asOpt[String]
        orderId <- Option((body \ "orderId").asOpt[Long])
        anonymous <- (body \ "anonymous").asOpt[Boolean] orElse Option(false)
        rating <- Option((body \ "rating").asOpt[Float])
        images <- (body \ "images").asOpt[Array[ImageItemTemp]] orElse None
      } yield {
        request.auth.user map (user => {
          CommodityAPI.postComment(commodityId, user, contents, rating, Option(images.toSeq), orderId, anonymous) map (_ => HanseResult.ok()) recover {
            case e: OrderStatusException => HanseResult.forbidden(errorMsg = Some(e.getMessage))
          }
        }) getOrElse {
          // 需要登录
          Future.successful(HanseResult.forbidden(errorMsg = Some("Posting comments requires authentication")))
        }
      }) getOrElse Future {
        HanseResult.unprocessable()
      }
    }
  )

  def getComment(commodityId: Long, start: Int, count: Int) = AuthenticatedAction.async2(
    request => {
      for {
        commodities <- CommodityAPI.getComments(commodityId, start, count)
      } yield {
        val node = CommodityCommentFormatter.instance.formatJsonNode(commodities)
        HanseResult(data = Some(node))
      }
    }
  )

}
