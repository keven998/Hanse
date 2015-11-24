package controllers

import javax.inject.{ Inject, Named }

import com.fasterxml.jackson.databind.JsonNode
import com.lvxingpai.inject.morphia.MorphiaMap
import core.api.CommodityAPI
import core.formatter.marketplace.product.{ CommodityFormatter, SimpleCommodityFormatter }
import core.misc.HanseResult
import play.api.Configuration
import play.api.mvc.{ Action, Controller }

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by pengyt on 2015/11/3.
 */
class CommodityCtrl @Inject() (@Named("default") configuration: Configuration, datastore: MorphiaMap) extends Controller {

  implicit val ds = datastore.map.get("k2").get

  /**
   * 根据商品的Id取得商品的详细信息
   * @param commodityId 商品Id
   * @return 商品详细信息
   */
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

  /**
   * 根据店铺id查找商品列表
   * @param sellerId 店铺id
   * @param sortBy 比如：按照销量排序
   * @param sort 正序或者逆序
   * @param start 返回商品列表的起始位置
   * @param count 返回商品的个数
   * @return 返回商品列表
   */
  def getCommodities(sellerId: Long, localityId: String, coType: String, sortBy: String, sort: String, start: Int, count: Int) = Action.async(
    request => {

      val commodityObjectMapper = new SimpleCommodityFormatter().objectMapper
      for {
        commodities <- CommodityAPI.getCommoditiesBySellerId(sellerId, sortBy, sort, start, count)
      } yield {
        val node = commodityObjectMapper.valueToTree[JsonNode](commodities)
        HanseResult(data = Some(node))
      }
    }
  )

  /**
   * 根据店铺id查找商品列表
   * @param localityId 店铺id
   * @param sortBy 比如：按照销量排序
   * @param sort 正序或者逆序
   * @param start 返回商品列表的起始位置
   * @param count 返回商品的个数
   * @return 返回商品列表
   */
  def getCommoditiesByLocalityNewId(localityId: String, sortBy: String, sort: String, start: Int, count: Int) = Action.async(
    request => {

      val commodityObjectMapper = new SimpleCommodityFormatter().objectMapper
      for {
        commodities <- CommodityAPI.getCommoditiesByLocalityId(localityId, sortBy, sort, start, count)
      } yield {
        val node = commodityObjectMapper.valueToTree[JsonNode](commodities)
        HanseResult(data = Some(node))
      }
    }
  )
}
