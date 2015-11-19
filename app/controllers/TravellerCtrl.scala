package controllers

import java.text.SimpleDateFormat

import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper }
import com.lvxingpai.model.account.Gender
import core.api.TravellerAPI
import core.formatter.misc.{ PersonFormatter, PersonParser }
import core.misc.HanseResult
import core.model.trade.order.Person
import play.api.mvc.{ Action, Controller }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by pengyt on 2015/11/16.
 */
class TravellerCtrl extends Controller {

  case class PreIdProof(number: String, nation: Option[Nation])
  case class Nation(id: String)
  /**
   * 添加旅客信息
   * @return
   */
  def addTraveller() = Action.async(
    request => {
      val node = new ObjectMapper().createObjectNode()
      val travellerMapper = new PersonFormatter().objectMapper
      val result = for {
        body <- request.body.asJson
        userId <- (body \ "userId").asOpt[Long]
      } yield {
        val person = PersonParser.apply(body.toString())
        for {
          traveller <- TravellerAPI.addTraveller(userId, person)
        } yield {
          node.put("key", traveller._1)
          node.set("traveller", travellerMapper.valueToTree[JsonNode](traveller._2))
          HanseResult(data = Some(node))
        }
      }
      if (result.nonEmpty) result.get else Future { HanseResult.unprocessable() }
    }
  )

  /**
   * 更新旅客信息
   * @return 旅客信息和key
   */
  def updateTraveller(key: String) = Action.async(
    request => {

      val person = new Person()
      val node = new ObjectMapper().createObjectNode()
      val ret = for {
        body <- request.body.asJson
        userId <- (body \ "userId").asOpt[Long]
        key <- (body \ "key").asOpt[String]
        surname <- (body \ "surname").asOpt[String]
        givenName <- (body \ "givenName").asOpt[String]
        gender <- (body \ "gender").asOpt[String]
        birthday <- (body \ "birthday").asOpt[String]
        idType <- (body \ "idType").asOpt[String]
        idProof <- (body \ "idProof").asOpt[String]
      } yield {
        person.surname = surname
        person.givenName = givenName
        person.gender = if (gender == "男") Gender.Male else Gender.Female
        person.birthday = new SimpleDateFormat("yyyy-MM-dd").parse(birthday)
        //          idType match {
        //            case "chineseID" => val i = new ChineseID
        //              i.number = idProof
        //              person.idProof = i
        //            case "passport" => val i = new Passport
        //              i.number = idProof
        //              i.nation = null
        //              person.idProof = i
        //          }
        userId -> person
      }
      if (ret.nonEmpty) {
        for {
          traveller <- TravellerAPI.updateTraveller(ret.get._1, key, ret.get._2)
        } yield {
          node.put("key", key)
          //            node.set("traveller", personNode)
          HanseResult(data = Some(node))
        }
      } else {
        Future {
          HanseResult(data = Some(node))
        }
      }

    }
  )

  /**
   * 删除旅客信息
   * @return key
   */
  def deleteTraveller(key: String) = Action.async(
    request => {

      val node = new ObjectMapper().createObjectNode()
      val ret = for {
        body <- request.body.asJson
        userId <- (body \ "userId").asOpt[Long]
      } yield {
        userId
      }
      if (ret.nonEmpty) {
        for {
          key <- TravellerAPI.deleteTraveller(ret.get, key)
        } yield {
          node.put("key", key)
          HanseResult(data = Some(node))
        }
      } else {
        Future {
          HanseResult(data = Some(node))
        }
      }
    }
  )

  /**
   * 获取旅客信息
   * @return key
   */
  def getTraveller(key: String) = Action.async(
    request => {

      val node = new ObjectMapper().createObjectNode()
      val ret = for {
        body <- request.body.asJson
        userId <- (body \ "userId").asOpt[Long]
      } yield {
        userId
      }
      if (ret.nonEmpty) {
        for {
          traveller <- TravellerAPI.getTraveller(ret.get, key)
        } yield {
          node.put("key", key)
          // node.set("traveller", travellerNode)
          HanseResult(data = Some(node))
        }
      } else {
        Future {
          HanseResult(data = Some(node))
        }
      }
    }
  )

  /**
   * 获取旅客信息列表
   * @return 旅客信息列表
   */
  def getTravellerList() = Action.async(
    request => {
      val arrayNode = new ObjectMapper().createArrayNode()
      val node = new ObjectMapper().createObjectNode()
      val ret = for {
        body <- request.body.asJson
        userId <- (body \ "userId").asOpt[Long]
      } yield {
        userId
      }
      if (ret.nonEmpty) {
        for {
          travellers <- TravellerAPI.getTravellerList(ret.get)
        } yield {
          travellers map (traveller => {
            node.put("key", traveller._1)
            // ....
            arrayNode.add(node)
          })
          HanseResult(data = Some(arrayNode))
        }
      } else {
        Future {
          HanseResult(data = Some(arrayNode))
        }
      }
    }
  )
}
