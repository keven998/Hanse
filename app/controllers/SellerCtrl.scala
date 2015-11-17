package controllers

import javax.inject.{ Inject, Named, Singleton }

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.yunkai.UserInfoProp
import core.api.SellerAPI
import core.formatter.marketplace.product.CommodityFormatter
import core.formatter.marketplace.seller.SellerFormatter
import core.misc.HanseResult
import core.model.SellerDTO
import play.api.Configuration
import play.api.mvc.{ Action, Controller }

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by topy on 2015/11/3.
 */
@Singleton
class SellerCtrl @Inject() (@Named("default") configuration: Configuration, datastore: MorphiaMap) extends Controller {

  import UserInfoProp._

  implicit lazy val ds = datastore.map.get("k2").get

  val fields = Seq(UserId, NickName, Avatar, Gender, Signature, Residence, Birthday)

  def getSeller(id: Long) = Action.async(
    request => {
      val selfId = request.headers.get("UserId") map (_.toLong)
      val sId = id
      val sellerFmt = (new SellerFormatter).objectMapper
      val ret = for {
        seller <- SellerAPI.getSeller(sId)
        //user <- FinagleFactory.client.getUserById(sId, Some(fields), selfId)
      } yield {
        val dto = SellerDTO(seller)
        val node = sellerFmt.valueToTree[JsonNode](dto)
        HanseResult(data = Some(node))
      }
      ret
    }
  )

  def getCommoditiesOfSeller(id: String) = Action.async(
    request => {
      val selfId = request.headers.get("UserId") map (_.toLong)
      val sId = id.toLong
      val sellerFmt = (new CommodityFormatter).objectMapper
      val ret = for {
        cmds <- SellerAPI.getCommoditiesBySeller(sId)
      } yield {
        val node = sellerFmt.valueToTree[ArrayNode](cmds)
        HanseResult(data = Some(node))
      }
      ret
    }
  )

}