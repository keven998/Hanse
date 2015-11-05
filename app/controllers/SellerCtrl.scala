package controllers

import javax.inject.Singleton

import com.lvxingpai.yunkai.UserInfoProp
import core.api.SellerAPI
import core.finagle.FinagleFactory
import play.api.mvc.{ Action, Controller }
import core.misc.Implicits._
import core.misc.Implicits.TwitterConverter._
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by topy on 2015/11/3.
 */
@Singleton
class SellerCtrl extends Controller {

  import UserInfoProp._

  val fields = Seq(UserId, NickName, Avatar, Gender, Signature, Residence, Birthday)

  def getSeller(id: String) = Action.async(
    request => {
      val selfId = request.headers.get("UserId") map (_.toLong)
      val sId = id.toLong
      val ret = for {
        seller <- SellerAPI.getSeller(sId)
        user <- FinagleFactory.client.getUserById(sId, Some(fields), selfId)
      } yield {
        null
      }
      ret
    }
  )

  def getCommoditiesOfSeller(id: String) = Action.async(
    request => {
      val selfId = request.headers.get("UserId") map (_.toLong)
      val sId = id.toLong
      val ret = for {
        cmds <- SellerAPI.getCommoditiesBySeller(sId)
      } yield {
        null
      }
      ret
    }
  )

}
