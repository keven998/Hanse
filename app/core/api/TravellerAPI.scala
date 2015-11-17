package core.api

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
}
