package controllers

import javax.inject.{ Inject, Named }

import com.lvxingpai.inject.morphia.MorphiaMap
import core.api.CommodityAPI
import core.formatter.marketplace.product.{ CommodityCategoryFormatter, CommodityFormatter, SimpleCommodityFormatter }
import core.misc.HanseResult
import play.api.Configuration
import play.api.mvc.{ Action, Controller }

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by topy on 2015/11/26.
 */
class CommodityCtrl @Inject() (@Named("default") configuration: Configuration, datastore: MorphiaMap) extends Controller {

  implicit val ds = datastore.map.get("k2").get

  def getCommodityDetail(commodityId: Long, version: Option[Long]) = Action.async(
    request => {
      for {
        commodity <- CommodityAPI.getCommodityById(commodityId, version)
      } yield {
        val node = CommodityFormatter.instance.formatJsonNode(commodity)
        HanseResult(data = Some(node))
      }
    }
  )

  def getCommodities(sellerId: Option[Long], locId: Option[String], category: Option[String], sortBy: String, sort: String, start: Int, count: Int) = Action.async(
    request => {
      for {
        commodities <- CommodityAPI.getCommodities(sellerId, locId, category, sortBy, sort, start, count)
      } yield {
        val node = SimpleCommodityFormatter.instance.formatJsonNode(commodities)
        HanseResult(data = Some(node))
      }
    }
  )

  def getCommodityCategory(locId: String) = Action.async(
    request => {
      for {
        commodities <- CommodityAPI.getCommodityCategories(locId)
      } yield {
        val base = Seq("全部")
        val cas = if (commodities == null) base else base ++ commodities.flatMap(_.category.asScala.toSeq).distinct
        val node = CommodityCategoryFormatter.instance.formatJsonNode(cas)
        HanseResult(data = Some(node))
      }
    }
  )

}
