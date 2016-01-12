package controllers

import javax.inject.{ Inject, Named }

import com.fasterxml.jackson.databind.node.ObjectNode
import com.lvxingpai.inject.morphia.MorphiaMap
import controllers.security.AuthenticatedAction
import core.api.{ MiscAPI, CommodityAPI }
import core.formatter.marketplace.product.{ CommodityCategoryFormatter, CommodityFormatter, SimpleCommodityFormatter }
import core.misc.HanseResult
import play.api.Configuration
import play.api.mvc.Controller

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by topy on 2015/11/26.
 */
class CommodityCtrl @Inject() (@Named("default") configuration: Configuration, datastore: MorphiaMap) extends Controller {

  implicit val ds = datastore.map.get("k2").get

  def getCommodityDetail(commodityId: Long, version: Option[Long]) = AuthenticatedAction.async2(
    request => {
      val userId = request.headers.get("UserId") map (_.toLong) getOrElse 0L
      for {
        commodity <- CommodityAPI.getCommodityById(commodityId, version)
        fas <- MiscAPI.getFavorite(userId, "commodity")
      } yield {
        if (commodity.nonEmpty) {
          val node = CommodityFormatter.instance.formatJsonNode(commodity.get).asInstanceOf[ObjectNode]
          node.put("isFavorite", fas.commodities contains commodity.get.id)
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
