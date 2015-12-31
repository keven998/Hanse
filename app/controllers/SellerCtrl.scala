package controllers

import javax.inject.{ Inject, Named, Singleton }

import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.yunkai.UserInfoProp
import core.api.SellerAPI
import core.formatter.marketplace.seller.SellerFormatter
import core.misc.HanseResult
import core.misc.Implicits.PhoneNumberTemp
import org.apache.commons.lang.StringUtils
import play.api.Configuration
import play.api.mvc.{ Action, Controller }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
      val ret = for {
        seller <- SellerAPI.getSeller(id)
        //user <- FinagleFactory.client.getUserById(sId, Some(fields), selfId)
      } yield {
        if (seller.nonEmpty)
          HanseResult(data = seller map (SellerFormatter.instance.formatJsonNode(_)))
        else
          HanseResult.notFound(Some(s"Seller not found. sellId is $id"))
      }
      ret
    }
  )

  def becomeSeller() = Action.async(
    request => {
      val ret = for {
        body <- request.body.asJson
        userId <- (body \ "userId").asOpt[Long]
        memo <- (body \ "memo").asOpt[String] orElse Some(StringUtils.EMPTY)
        email <- (body \ "email").asOpt[String] orElse Some(StringUtils.EMPTY)
        tel <- (body \ "tel").asOpt[PhoneNumberTemp]
      } yield {
        Future(HanseResult.ok())
      }
      ret.getOrElse(Future {
        HanseResult.unprocessable()
      })
    }
  )

}
