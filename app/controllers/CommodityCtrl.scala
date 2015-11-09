package controllers

import javax.inject.{ Named, Inject }

import com.fasterxml.jackson.databind.JsonNode
import com.lvxingpai.inject.morphia.MorphiaMap
import core.api.CommodityAPINew
import core.misc.HanseResult
import play.api.Configuration
import play.api.mvc.{ Action, Controller }
import scala.concurrent.ExecutionContext.Implicits.global
import core.formatter.marketplace.product.CommodityFormatter

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
        cmy <- CommodityAPINew.getCommodityById(commodityId)
      } yield {
        val node = commodityMapper.valueToTree[JsonNode](cmy)
        HanseResult(data = Some(node))
      }
    }
  )
}
