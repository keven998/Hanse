package controllers

import javax.inject.{ Inject, Named }

import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper }
import com.lvxingpai.inject.morphia.MorphiaMap
import core.api.MiscAPI
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
  def getColumns = Action.async(
    request => {
      val arrayNode = new ObjectMapper().createArrayNode()
      val columnMapper = new ColumnFormatter().objectMapper
      val columnTypes = Seq("slide", "special")
      for {
        columnsMap <- MiscAPI.getColumns(columnTypes)
      } yield {
        columnTypes map (columnType => {
          val node = new ObjectMapper().createObjectNode()
          node.put("columnType", columnType)
          node.set("columns", columnMapper.valueToTree[JsonNode](columnsMap(columnType).sortBy(_.rank)))
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
      val node = new ObjectMapper().createObjectNode()
      for {
        commodities <- MiscAPI.getCommoditiesByTopic(topicType)
      } yield {
        node.put("topicType", topicType)
        node.set("commodities", SimpleCommodityFormatter.instance.formatJsonNode(commodities))
        HanseResult(data = Some(node))
      }
    }
  )

  /**
   * 查找推荐商品列表
   * @return 商品列表
   */
  def getRecommendCommodities = Action.async(
    request => {
      val arrayNode = new ObjectMapper().createArrayNode()
      for {
        categories <- MiscAPI.getRecommendCategories()
        topicCommoditiesMap <- MiscAPI.getRecommendCommodities(categories)
      } yield {
        categories map (topic => {
          val commodities = topicCommoditiesMap(topic)
          val node = new ObjectMapper().createObjectNode()
          node.put("topicType", topic)
          node.set("commodities", SimpleCommodityFormatter.instance.formatJsonNode(commodities))
          arrayNode.add(node)
        })
        HanseResult(data = Some(arrayNode))
      }
    }
  )

}
