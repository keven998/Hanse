package controllers

import javax.inject.{ Inject, Named }

import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper }
import com.lvxingpai.inject.morphia.MorphiaMap
import core.api.{ CommodityAPI, MiscAPI }
import core.formatter.marketplace.product.SimpleCommodityFormatter
import core.formatter.misc.ColumnFormatter
import core.misc.HanseResult
import play.api.Configuration
import play.api.mvc.{ Action, Controller }

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by pengyt on 2015/11/13.
 */
class MiscCtrl @Inject() (@Named("default") configuration: Configuration, datastore: MorphiaMap) extends Controller {

  implicit lazy val ds = datastore.map.get("k2").get

  /**
   * 首页专题
   * @return
   */
  def getColumns() = Action.async(
    request => {
      val arrayNode = new ObjectMapper().createArrayNode()
      val columnMapper = new ColumnFormatter().objectMapper
      val columnTypes = Seq("slide", "special")
      for {
        columnsMap <- MiscAPI.getColumns(columnTypes) //"slide")
      } yield {
        columnTypes map (columnType => {
          val node = new ObjectMapper().createObjectNode()
          node.put("columnType", columnType)
          node.set("columns", columnMapper.valueToTree[JsonNode](columnsMap(columnType)))
          arrayNode.add(node)
        })
        HanseResult(data = Some(arrayNode))
      }
    }
  )

  /**
   * 根据话题类型查找商品列表
   * @param topicType 话题类型
   * @return 商品列表
   */
  def getCommoditiesByTopic(topicType: String) = Action.async(
    request => {
      val simpleCommodityMapper = new SimpleCommodityFormatter().objectMapper
      val node = new ObjectMapper().createObjectNode()
      for {
        commodities <- MiscAPI.getCommoditiesByTopic(topicType)
      } yield {
        node.put("topicType", topicType)
        node.set("commodities", simpleCommodityMapper.valueToTree[JsonNode](commodities))
        HanseResult(data = Some(node))
      }
    }
  )

  /**
   * 查找推荐商品列表
   * @return 商品列表
   */
  def getRecommendCommodities() = Action.async(
    request => {
      val arrayNode = new ObjectMapper().createArrayNode()
      val simpleCommodityMapper = new SimpleCommodityFormatter().objectMapper
      for {
        categories <- MiscAPI.getRecommendCategories()
        topicCommoditiesMap <- MiscAPI.getRecommendCommodities(categories)
      } yield {
        categories map (topic => {
          val commodities = topicCommoditiesMap(topic)
          val node = new ObjectMapper().createObjectNode()
          node.put("topicType", topic)
          node.set("commodities", simpleCommodityMapper.valueToTree[JsonNode](commodities))
          arrayNode.add(node)
        })
        HanseResult(data = Some(arrayNode))
      }
    }
  )

  /**
   * 根据目的地id查找商品分类。对目的地的所有商品遍历查找商品的去重分类，放置缓存中，每隔一段时间更新一次缓存
   * @param localityId 目的地id
   * @return 分类列表
   */
  def getCommodityCategoryList(localityId: String) = Action.async(
    request => {
      val node = new ObjectMapper().createObjectNode()
      for {
        category <- CommodityAPI.getCommodityCategoryList(localityId)
      } yield {
        node.put("locality", localityId)
        node.set("category", new ObjectMapper().valueToTree(category))
        HanseResult(data = Some(node))
      }
    }
  )
}
