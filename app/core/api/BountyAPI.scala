package core.api

import java.util.Date

import com.lvxingpai.model.account.RealNameInfo
import com.lvxingpai.model.geo.Locality
import com.lvxingpai.model.marketplace.order.Bounty
import com.lvxingpai.model.marketplace.product.Schedule
import com.lvxingpai.model.marketplace.seller.Seller
import com.lvxingpai.yunkai.{UserInfo => YunkaiUser}
import core.exception.ResourceNotFoundException
import org.joda.time.DateTime
import org.mongodb.morphia.Datastore

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

/**
 * Created by topy on 2016/3/29.
 */
object BountyAPI {

  def createBounty(userId: Long, contact: RealNameInfo, destination: Seq[Locality], departure: Locality, departureDate: Date, timeCost: Int,
    participantCnt: Int, perBudget: Int, participants: Seq[String],
    service: String, topic: String, memo: String, totalPrice: Int)(implicit ds: Datastore): Future[Bounty] = {
    val bounty = new Bounty()
    val now = DateTime.now().toDate
    bounty.itemId = now.getTime
    bounty.createTime = now
    bounty.updateTime = now
    bounty.contact = contact
    bounty.departure = departure
    bounty.departureDate = departureDate
    bounty.timeCost = timeCost
    bounty.participants = participants
    bounty.budget = perBudget
    bounty.destination = destination
    bounty.service = service
    bounty.topic = topic
    bounty.memo = memo
    bounty.consumerId = userId
    bounty.totalPrice = totalPrice
    Future {
      ds.save[Bounty](bounty)
      bounty
    }
  }

  def getBounties()(implicit ds: Datastore): Future[Seq[Bounty]] = {
    Future {
      ds.createQuery(classOf[Bounty]).asList()
    }
  }

  def getSchedule(bountyId: Long)(implicit ds: Datastore): Future[Seq[Schedule]] = {
    Future {
      val bounty = Option(ds.createQuery(classOf[Bounty]) field "itemId" equal bountyId get)
      bounty match {
        case None => Seq()
        case x => x.get.schedules
      }
    }
  }

  def addSchedule(bountyId: Long, seller: Option[Seller], desc: String, price: Int)(implicit ds: Datastore): Future[Unit] = {
    if (seller.isEmpty)
      throw ResourceNotFoundException(s"Cannot find seller.")
    Future {
      val sc = new Schedule
      val now = DateTime.now().toDate
      sc.desc = desc
      sc.createTime = now
      sc.updateTime = now
      sc.price = price
      sc.seller = seller.get
      sc.itemId = now.getTime
      sc.title = "日程安排"
      val statusQuery = ds.createQuery(classOf[Bounty]) field "itemId" equal bountyId
      val statusOps = ds.createUpdateOperations(classOf[Bounty]).add("schedules", sc)
      ds.update(statusQuery, statusOps)
    }
  }

}
