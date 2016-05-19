package controllers

import javax.inject.{ Inject, Named, Singleton }

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper }
import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.yunkai.UserInfoProp
import core.api._
import core.formatter.geo.{ CountryFormatter, GeoCommodityFormatter, LocalityFormatter }
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
    request => {
      for {
        country <- GeoAPI.getCountryById(new ObjectId(id))
        sellers <- CommodityAPI.getGeoSeller(id)
      } yield {
        val node = CountryFormatter.instance.formatJsonNode(country).asInstanceOf[ObjectNode]
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
      val arrayNode = new ObjectMapper().createArrayNode()
      val mapper = new LocalityArticleFormatter().objectMapper
      val fields = Seq("zhName", "enName", "desc", "travelMonth", "images", "remarks")
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
