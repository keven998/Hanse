package controllers

import javax.inject.{ Inject, Named }

import com.fasterxml.jackson.databind.node.ObjectNode
import com.lvxingpai.inject.morphia.MorphiaMap
import controllers.security.AuthenticatedAction
import core.api.{ SellerAPI, CommodityAPI, MiscAPI }
import core.formatter.marketplace.product.{ CommodityCategoryFormatter, CommodityFormatter, SimpleCommodityFormatter }
import core.misc.HanseResult
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

}
