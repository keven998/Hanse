package controllers

import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper }
import core.api.MiscAPI
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
        columnsSlide <- MiscAPI.getColumns("slide")
        columnsSpecial <- MiscAPI.getColumns("special")
      } yield {
        columnSlideGroup.columnType = "slide"
        columnSlideGroup.columns = columnsSlide
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
}
