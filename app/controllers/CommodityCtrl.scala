package controllers

import javax.inject.{ Inject, Named }

import com.fasterxml.jackson.databind.JsonNode
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

  def getCommodityDetail(commodityId: Long) = Action.async(
    request => {
      val commodityMapper = (new CommodityFormatter).objectMapper
      for {
        commodity <- CommodityAPI.getCommodityById(commodityId)
      } yield {
        val node = commodityMapper.valueToTree[JsonNode](commodity)
        HanseResult(data = Some(node))
      }
    }
  )

  def getCommodities(sellerId: Option[Long], locId: Option[String], category: Option[String], sortBy: String, sort: String, start: Int, count: Int) = Action.async(
    request => {

      val commodityObjectMapper = new SimpleCommodityFormatter().objectMapper
      for {
        commodities <- CommodityAPI.getCommodities(sellerId, locId, category, sortBy, sort, start, count)
      } yield {
        val node = commodityObjectMapper.valueToTree[JsonNode](commodities)
        HanseResult(data = Some(node))
      }
    }
  )

  def getCommodityCategory(locId: String) = Action.async(
    request => {
      val categoryMapper = new CommodityCategoryFormatter().objectMapper
      for {
        commodities <- CommodityAPI.getCommodityCategories(locId)
      } yield {
        val base = Seq(CommodityAPI.COMMODITY_CATEGORY_ALL)
        val cas = if (commodities == null) base else base ++ commodities.map(_.category.asScala.toSeq).flatten.distinct
        val node = categoryMapper.valueToTree[JsonNode](cas)
        HanseResult(data = Some(node))
      }
    }
  )

}
