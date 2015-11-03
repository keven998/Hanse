package controllers

import core.api.CommodityAPI
import play.api.mvc.{ Action, Controller }

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by pengyt on 2015/11/3.
 */
class CommodityCtrl extends Controller {

  /**
   * 根据商品的Id取得商品的详细信息
   * @param commodityId 商品Id
   * @return 商品详细信息
   */
  def getCommodityDetail(commodityId: String) = Action.async(
    request => {
      val cmyFormatter = null
      val cmyInfo = for{
        cmy <- CommodityAPI.getCommodityById(commodityId)
      } yield cmy

      Future {
        null
      }
    })
}
