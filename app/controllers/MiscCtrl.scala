package controllers

import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper }
import core.api.{ CommodityAPINew, MiscAPI }
import core.formatter.marketplace.product.SimpleCommodityFormatter
import core.formatter.misc.ColumnGroupFormatter
import core.misc.HanseResult
import core.model.misc.ColumnGroup
import play.api.mvc.{ Action, Controller }

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by pengyt on 2015/11/13.
 */
class MiscCtrl extends Controller {

  /**
   * 首页专题
   * @return
   */
  def getColumns() = Action.async(
    request => {
      val columnGroupMapper = new ColumnGroupFormatter().objectMapper
      val columnSlideGroup = new ColumnGroup
      val columnSpecialGroup = new ColumnGroup
      for {
        columns <- MiscAPI.getColumns() //"slide")
      } yield {
        val columnsSlide = columns.filter(_.columnType.equalsIgnoreCase("slide"))
        columnSlideGroup.columnType = "slide"
        columnSlideGroup.columns = columnsSlide
        val columnsSpecial = columns.filter(_.columnType.equalsIgnoreCase("special"))
        columnSpecialGroup.columnType = "special"
        columnSpecialGroup.columns = columnsSpecial
        val node = columnGroupMapper.valueToTree[JsonNode](Seq(columnSlideGroup, columnSpecialGroup))
        HanseResult(data = Some(node))
      }
    }
  )

  /**
   * 根据话题类型查找商品列表
   * @param topicType 话题类型
   * @return 商品列表
   */
  def getSpecialCommodities(topicType: String) = Action.async(
    request => {
      val simpleCommodityMapper = new SimpleCommodityFormatter().objectMapper
      val node = new ObjectMapper().createObjectNode()
      for {
        commodities <- MiscAPI.getSpecialCommodities(topicType)
      } yield {
        if (commodities != null && commodities.nonEmpty) {
          node.put("topicTitle", commodities.head.topicTitle)
          node.set("commodities", simpleCommodityMapper.valueToTree[JsonNode](commodities))
          HanseResult(data = Some(node))
        } else {
          HanseResult.unprocessable()
        }
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
        discountCommodities <- MiscAPI.getSpecialCommodities("discount")
        editRecommendCommodities <- MiscAPI.getSpecialCommodities("editRecommend")
        hotPlayCommodities <- MiscAPI.getSpecialCommodities("hotPlay")
      } yield {
        if (discountCommodities != null && discountCommodities.nonEmpty) {
          val node = new ObjectMapper().createObjectNode()
          node.put("recommendType", "discount")
          node.put("topicTitle", discountCommodities.head.topicTitle)
          node.set("commodities", simpleCommodityMapper.valueToTree[JsonNode](discountCommodities))
          arrayNode.add(node)
        }
        if (editRecommendCommodities != null && editRecommendCommodities.nonEmpty) {
          val node = new ObjectMapper().createObjectNode()
          node.put("recommendType", "editRecommend")
          node.put("topicTitle", editRecommendCommodities.head.topicTitle)
          node.set("commodities", simpleCommodityMapper.valueToTree[JsonNode](editRecommendCommodities))
          arrayNode.add(node)
        }
        if (hotPlayCommodities != null && hotPlayCommodities.nonEmpty) {
          val node = new ObjectMapper().createObjectNode()
          node.put("recommendType", "hotPlay")
          node.put("topicTitle", hotPlayCommodities.head.topicTitle)
          node.set("commodities", simpleCommodityMapper.valueToTree[JsonNode](hotPlayCommodities))
          arrayNode.add(node)
        }
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
        category <- CommodityAPINew.getCommodityCategoryList(localityId)
      } yield {
        node.put("locality", localityId)
        node.set("category", new ObjectMapper().valueToTree(category))
        HanseResult(data = Some(node))
      }
    }
  )
}
