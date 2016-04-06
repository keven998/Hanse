package controllers

import javax.inject.{ Inject, Named }

import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.model.account.RealNameInfo
import controllers.security.AuthenticatedAction
import core.api.{ BountyAPI, SellerAPI }
import core.exception.ResourceNotFoundException
import core.formatter.marketplace.order.{ BountyFormatter, ScheduleFormatter, SimpleBountyFormatter, TravellersFormatter }
import core.misc.HanseResult
import core.misc.Implicits._
import core.service.ViaeGateway
import org.joda.time.DateTime
import play.api.libs.json.JsDefined
import play.api.mvc.Controller
import play.api.{ Configuration, Play }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * 对于用户悬赏的相关操作
 *
 * Created by topy on 2016/3/29.
 */
class BountyCtrl @Inject() (@Named("default") configuration: Configuration, datastore: MorphiaMap,
    implicit val viaeGateway: ViaeGateway) extends Controller {

  import com.lvxingpai.yunkai.Userservice.{ FinagledClient => YunkaiClient }
  import play.api.Play.current

  val yunkai = Play.application.injector instanceOf classOf[YunkaiClient]

  implicit lazy val ds = datastore.map.get("k2").get

  /**
   * 创建悬赏
   *
   * @return 返回订单信息
   */
  def createBounty() = AuthenticatedAction.async2(
    request => {
      val userId = (request.headers get "X-Lvxingpai-Id" getOrElse "").toLong
      val ret = for {
        body <- request.body.wrapped.asJson
        destination <- (body \ "destination").asOpt[Array[TempLocality]]
        departure <- (body \ "departure").asOpt[TempLocality]
        departureDate <- (body \ "departureDate").asOpt[String]
        timeCost <- (body \ "timeCost").asOpt[Int]
        participantCnt <- (body \ "participantCnt").asOpt[Int]
        participants <- (body \ "participants").asOpt[Seq[String]] orElse Option(Seq())
        perBudget <- (body \ "budget").asOpt[Int]
        service <- (body \ "service").asOpt[String]
        topic <- (body \ "topic").asOpt[String]
        memo <- (body \ "memo").asOpt[String]
        totalPrice <- (body \ "totalPrice").asOpt[Int]
      } yield {
        // 出发时间
        val date = DateTime.parse(departureDate).toDate
        val contact = TravellersFormatter.instance.parse[RealNameInfo]((body \ "contact").asInstanceOf[JsDefined].value.toString())
        for {
          bounty <- BountyAPI.createBounty(userId, contact, destination.toSeq, departure, date, timeCost, participantCnt, perBudget, participants,
            service, topic, memo, totalPrice)
        } yield HanseResult(data = Some(BountyFormatter.instance.formatJsonNode(bounty)))
      } recover {
        case e: ResourceNotFoundException => HanseResult.unprocessable(errorMsg = Some(e.getMessage))
      }
      ret getOrElse Future {
        HanseResult.unprocessable()
      }
    }
  )

  /**
   * 取得悬赏列表
   *
   * @return 返回订单信息
   */
  def getBounties(userId: Option[Long]) = AuthenticatedAction.async2(
    request => {
      val ret = for {
        bounty <- BountyAPI.getBounties(userId)
      } yield {
        HanseResult(data = Some(SimpleBountyFormatter.instance.formatJsonNode(bounty)))
      }
      ret
    }
  )

  def getMyBounties(userId: Long) = getBounties(Some(userId))

  /**
   * 添加日程安排
   *
   * @param bountyId
   * @return
   */
  def addSchedule(bountyId: Long) = AuthenticatedAction.async2(
    request => {
      val userId = (request.headers get "X-Lvxingpai-Id" getOrElse "").toLong
      val ret = for {
        body <- request.body.wrapped.asJson
        desc <- (body \ "desc").asOpt[String]
        topic <- (body \ "guideId").asOpt[String]
        price <- (body \ "price").asOpt[Int]
      } yield {
        for {
          seller <- SellerAPI.getSeller(userId)
          _ <- BountyAPI.addSchedule(bountyId, seller, desc, price)
        } yield HanseResult.ok()
      } recover {
        case e: ResourceNotFoundException => HanseResult.unprocessable(errorMsg = Some(e.getMessage))
      }
      ret getOrElse Future {
        HanseResult.unprocessable()
      }
    }
  )

  /**
   * 取得某个悬赏的所有回复
   *
   * @param bountyId
   * @return
   */
  def getSchedules(bountyId: Long) = AuthenticatedAction.async2(
    request => {
      val ret = for {
        schedules <- BountyAPI.getSchedule(bountyId)
      } yield HanseResult(data = Some(ScheduleFormatter.instance.formatJsonNode(schedules)))
      ret
    }
  )

}
