package controllers

import javax.inject.{ Inject, Named }

import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper }
import com.lvxingpai.inject.morphia.MorphiaMap
import controllers.security.AuthenticatedAction
import core.api.{ CommodityAPI, MiscAPI }
import core.formatter.marketplace.product.SimpleCommodityFormatter
import core.formatter.misc.ColumnFormatter
import core.misc.HanseResult
import play.api.Configuration
import play.api.inject.Injector
import play.api.mvc._

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by pengyt on 2015/11/13.
 */
class MiscCtrl @Inject() (@Named("default") configuration: Configuration, datastore: MorphiaMap, injector: Injector) extends Controller {

  implicit lazy val ds = datastore.map.get("k2").get

  /**
   * 首页专题
   * @return
   */
  def getColumns = AuthenticatedAction.async2(
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
  def getCommoditiesByTopic(topicType: String) = AuthenticatedAction.async2(
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
  def getRecommendCommodities = AuthenticatedAction.async2(
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

  def getFavorite(userId: Long, itemType: String) = Action.async(
    request => {
      val node = new ObjectMapper().createObjectNode()
      val commodityFields = Seq("id", "commodityId", "price", "marketPrice", "title", "cover")
      for {
        fas <- MiscAPI.getFavorite(userId, itemType)
        commodities <- CommodityAPI.getCommoditiesByObjectIdList(fas map (_.commodities.toSeq) getOrElse Seq(), commodityFields)
      } yield {
        node.set("commodities", SimpleCommodityFormatter.instance.formatJsonNode(commodities getOrElse Seq()))
        HanseResult(data = Some(node))
      }
    }
  )

  def addFavorite(userId: Long) = Action.async(
    request => {
      val ret = for {
        body <- request.body.asJson
        itemId <- (body \ "itemId").asOpt[String]
        itemType <- (body \ "itemType").asOpt[String]
      } yield {
        for {
          tls <- MiscAPI.addFavorite(userId, itemType, itemId)
        } yield {
          HanseResult.ok()
        }
      }
      ret.getOrElse(Future {
        HanseResult.unprocessable()
      })
    }
  )

  def delFavorite(userId: Long, itemType: String, itemId: String) = Action.async(
    request => {
      for {
        tls <- MiscAPI.delFavorite(userId, itemType, itemId)
      } yield {
        HanseResult.ok()
      }
    }
  )

  def ping() = Action(Results.Ok("pong"))
}
