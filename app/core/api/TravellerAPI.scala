package core.api

import com.lvxingpai.model.geo.Country
import core.db.MorphiaFactory
import core.model.account.UserInfo
import core.model.trade.order.Person
import org.bson.types.ObjectId

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by pengyt on 2015/11/16.
 */
object TravellerAPI {

  val ds = MorphiaFactory.datastore

  /**
   * 添加旅客信息
   * @param userId 用户id
   * @param person 旅客信息
   * @return 旅客键值和旅客信息
   */
  def addTraveller(userId: Long, person: Person): Future[(String, Person)] = {
    val query = ds.createQuery(classOf[UserInfo])
    val key = new ObjectId().toString

    val ops = ds.createUpdateOperations(classOf[UserInfo]).add("travellers", key -> person, false)
    Future {
      ds.findAndModify(query, ops, false, true)
      key -> person
    }
  }

  /**
   * 修改旅客信息
   * @param userId 用户id
   * @param person 旅客信息
   * @return 旅客键值和旅客信息
   */
  def updateTraveller(userId: Long, key: String, person: Person): Future[(String, Person)] = {

    val query = ds.createQuery(classOf[UserInfo])
    val opsRm = ds.createUpdateOperations(classOf[UserInfo]).removeAll("travellers", key -> person)
    val opsAdd = ds.createUpdateOperations(classOf[UserInfo]).add("travellers", key -> person, false)
    Future {
      ds.findAndModify(query, opsRm, false, true)
      ds.findAndModify(query, opsAdd, false, true)
      key -> person
    }
  }

  /**
   * 删除旅客信息
   * @param userId 用户id
   * @param key 旅客信息键值
   * @return 空
   */
  def deleteTraveller(userId: Long, key: String): Future[String] = {

    val query = ds.createQuery(classOf[UserInfo])
    val opsRm = ds.createUpdateOperations(classOf[UserInfo]).removeAll("travellers", key)
    Future {
      ds.updateFirst(query, opsRm)
      key
    }
  }

  /**
   * 根据用户id和旅客键值取得旅客信息
   * @param userId 用户id
   * @param key 旅客信息键值
   * @return 旅客信息
   */
  def getTraveller(userId: Long, key: String): Future[Person] = {

    val query = ds.createQuery(classOf[UserInfo]).field("userId").equal(userId)

    Future {
      query.get().travellers(key)
    }
  }

  /**
   * 根据用户id取得所有旅客信息
   * @param userId 用户id
   * @return 旅客信息列表
   */
  def getTravellerList(userId: Long): Future[Map[String, Person]] = {

    val query = ds.createQuery(classOf[UserInfo]).field("userId").equal(userId)
    Future {
      query.get.travellers
    }
  }

  /**
   * 根据国家id取得国家信息
   */
  def getCountryById(id: ObjectId): Future[Country] = {
    val query = ds.createQuery(classOf[Country]).field("id").equal(id)
    Future {
      query.get
    }
  }
}
