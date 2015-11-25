package controllers

import javax.inject.{ Inject, Named, Singleton }

import com.fasterxml.jackson.databind.JsonNode
import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.yunkai.UserInfoProp
import core.api.SellerAPI
import core.formatter.marketplace.seller.SellerFormatter
import core.misc.HanseResult
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
      val sellerFmt = (new SellerFormatter).objectMapper
      val ret = for {
        seller <- SellerAPI.getSeller(id)
        //user <- FinagleFactory.client.getUserById(sId, Some(fields), selfId)
      } yield {
        val result = if (seller.nonEmpty) Some(sellerFmt.valueToTree[JsonNode](seller.get)) else None
        HanseResult(data = result)
      }
      ret
    }
  )

}
