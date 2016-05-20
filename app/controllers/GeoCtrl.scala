package controllers

import javax.inject.{ Inject, Named, Singleton }

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.model.geo.Country
import com.lvxingpai.yunkai.UserInfoProp
import core.api._
import core.formatter.geo._
import core.formatter.misc.LocalityArticleFormatter
import core.misc.HanseResult
import org.bson.types.ObjectId
import play.api.Configuration
import play.api.mvc.{ Action, Controller }

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by topy on 2015/11/3.
 */
@Singleton
class GeoCtrl @Inject() (@Named("default") configuration: Configuration, datastore: MorphiaMap) extends Controller {

  import UserInfoProp._

  implicit lazy val ds = datastore.map.get("k2").get

  val fields = Seq(UserId, NickName, Avatar, Gender, Signature, Residence, Birthday)

  def getCountry(id: String) = Action.async(
    block = request => {
    val mapper = new SimpleCountryFormatter().objectMapper
    val aMapper = new LocalityArticleFormatter().objectMapper
    for {
      country <- GeoAPI.getCountryById(new ObjectId(id))
      sellers <- CommodityAPI.getGeoSeller(id)
      arts <- GeoAPI.getArticleByLocalityId(new ObjectId(id))
    } yield {
      val node = CountryFormatter.instance.formatJsonNode(country).asInstanceOf[ObjectNode]
      // TODO 适配前端逻辑
      val countryTemp = new Country
      countryTemp.id = country.id
      countryTemp.zhName = country.zhName
      node.set("country", mapper.valueToTree[JsonNode](countryTemp))
      node.set("remarks", aMapper.valueToTree[JsonNode](arts))
      HanseResult(data = Some(node))
    }
  }
  )

  def getGeoSellers(id: String, countryType: String) = Action.async(
    request => {
      for {
        sellers <- CommodityAPI.getGeoSeller(id)
      } yield {
        HanseResult(data = Some(GeoCommodityFormatter.instance.formatJsonNode(sellers)))
      }
    }
  )

  def getLocality(id: String) = Action.async(
    request => {
      val mapper = new LocalityArticleFormatter().objectMapper
      val fields = Seq("zhName", "enName", "desc", "travelMonth", "images", "country")
      for {
        locality <- GeoAPI.getLocalityById(new ObjectId(id), Option(fields))
        arts <- GeoAPI.getArticleByLocalityId(new ObjectId(id))
      } yield {
        val node = LocalityFormatter.instance.formatJsonNode(locality).asInstanceOf[ObjectNode]
        node.set("remarks", mapper.valueToTree[JsonNode](arts))
        HanseResult(data = Some(node))
      }
    }
  )

  def countSellerInCountry() = Action.async(
    request => {
      for {
        all <- CommodityAPI.getAllCommodities()
        _ <- CommodityAPI.saveCountrySellers(all)
        _ <- CommodityAPI.saveLocalitySellers(all)
      } yield {
        HanseResult.ok()
      }
    }
  )

}
