package controllers

import javax.inject.{ Inject, Named }

import com.fasterxml.jackson.databind.ObjectMapper
import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.model.account.RealNameInfo
import core.api.TravellerAPI
import core.formatter.marketplace.order.TravellersFormatter
import core.misc.HanseResult
import play.api.Configuration
import play.api.mvc.{ Action, Controller }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by pengyt on 2015/11/16.
 */
class TravellerCtrl @Inject() (@Named("default") configuration: Configuration, datastore: MorphiaMap) extends Controller {

  implicit lazy val ds = datastore.map.get("k2").get

  /**
   * 添加旅客信息
   * @return
   */
  def addTraveller(userId: Long) = Action.async(
    request => {
      val node = new ObjectMapper().createObjectNode()
      val result = for {
        body <- request.body.asJson
      } yield {
        val person = TravellersFormatter.instance.parse[RealNameInfo](body.toString())
        for {
          traveller <- TravellerAPI.addTraveller(userId, person)
        } yield {
          node.put("key", traveller._1)
          node.set("traveller", TravellersFormatter.instance.formatJsonNode(traveller._2))
          HanseResult(data = Some(node))
        }
      }
      if (result.nonEmpty) result.get
      else Future {
        HanseResult.unprocessable()
      }
    }
  )

  /**
   * 更新旅客信息
   * @return 旅客信息和key
   */
  def updateTraveller(key: String, userId: Long) = Action.async(
    block = request => {
    val node = new ObjectMapper().createObjectNode()
    val result = for {
      body <- request.body.asJson
    } yield {
      val person = TravellersFormatter.instance.parse[RealNameInfo](body.toString())
      for {
        traveller <- TravellerAPI.updateTraveller(userId, key, person)
        ret <- TravellerAPI.getTraveller(userId, key)
      } yield {
        node.put("key", key)
        node.set("traveller", TravellersFormatter.instance.formatJsonNode(ret.get))
        HanseResult(data = Some(node))
      }
    }
    if (result.nonEmpty) result.get
    else Future {
      HanseResult.unprocessable()
    }
  }
  )

  /**
   * 删除旅客信息
   * @return key
   */
  def deleteTraveller(key: String, userId: Long) = Action.async(
    request => {
      Future {
        TravellerAPI.deleteTraveller(userId, key)
        HanseResult.ok()
      }
    }
  )

  /**
   * 获取旅客信息
   * @return key
   */
  def getTraveller(key: String, userId: Long) = Action.async(
    request => {
      val node = new ObjectMapper().createObjectNode()
      for {
        traveller <- TravellerAPI.getTraveller(userId, key)
      } yield {
        node.put("key", key)
        node.set("traveller", TravellersFormatter.instance.formatJsonNode(traveller))
        HanseResult(data = Some(node))
      }
    }
  )

  /**
   * 获取旅客信息列表
   * @return 旅客信息列表
   */
  def getTravellerList(userId: Long) = Action.async(
    request => {
      val arrayNode = new ObjectMapper().createArrayNode()
      for {
        travellers <- TravellerAPI.getTravellerList(userId)
      } yield {
        travellers map (traveller => {
          val node = new ObjectMapper().createObjectNode()
          node.put("key", traveller._1)
          node.set("traveller", TravellersFormatter.instance.formatJsonNode(traveller._2))
          arrayNode.add(node)
        })
        HanseResult(data = Some(arrayNode))
      }
    }
  )
}
