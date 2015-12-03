package core.api

import com.lvxingpai.model.account.{ RealNameInfo, UserInfo }
import org.bson.types.ObjectId
import org.mongodb.morphia.Datastore

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by pengyt on 2015/11/16.
 */
object TravellerAPI {

  /**
   * 添加旅客信息
   * @param userId 用户id
   * @param person 旅客信息
   * @return 旅客键值和旅客信息
   */
  def addTraveller(userId: Long, person: RealNameInfo)(implicit ds: Datastore): Future[(String, RealNameInfo)] = {
    Future {
      val query = ds.createQuery(classOf[UserInfo]).field("userId").equal(userId)
      val key = new ObjectId().toString
      val ops = ds.createUpdateOperations(classOf[UserInfo]).set(s"travellers.$key", person)
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
  def updateTraveller(userId: Long, key: String, person: RealNameInfo)(implicit ds: Datastore) = {
    val query = ds.createQuery(classOf[UserInfo]).field("userId").equal(userId)
    val ops = ds.createUpdateOperations(classOf[UserInfo]).set(s"travellers.$key", person)
    Future {
      ds.updateFirst(query, ops)
    }
  }

  /**
   * 删除旅客信息
   * @param userId 用户id
   * @param key 旅客信息键值
   * @return 空
   */
  def deleteTraveller(userId: Long, key: String)(implicit ds: Datastore): Future[Unit] = {
    val query = ds.createQuery(classOf[UserInfo]).field("userId").equal(userId)
    val opsRm = ds.createUpdateOperations(classOf[UserInfo]).unset(s"travellers.$key")
    Future {
      ds.updateFirst(query, opsRm)
    }
  }

  /**
   * 根据用户id和旅客键值取得旅客信息
   * @param userId 用户id
   * @param key 旅客信息键值
   * @return 旅客信息
   */
  def getTraveller(userId: Long, key: String)(implicit ds: Datastore): Future[Option[RealNameInfo]] = {
    val query = ds.createQuery(classOf[UserInfo]).field("userId").equal(userId)
    Future {
      val userInfo = query.get()
      if (userInfo == null || userInfo.travellers == null)
        None
      else if (!userInfo.travellers.containsKey(key))
        None
      else
        Some(userInfo.travellers(key))
    }
  }

  def getTravellerByKeys(userId: Long, key: Seq[String])(implicit ds: Datastore): Future[Option[Map[String, RealNameInfo]]] = {
    val query = ds.createQuery(classOf[UserInfo]).field("userId").equal(userId)
    Future {
      val userInfo = query.get()
      if (userInfo == null || userInfo.travellers == null) None
      else {
        val ret = key.filter(userInfo.travellers.containsKey(_)).map(k => {
          k -> userInfo.travellers(k)
        }).toMap
        Option(ret)
      }
    }
  }

  /**
   * 根据用户id取得所有旅客信息
   * @param userId 用户id
   * @return 旅客信息列表
   */
  def getTravellerList(userId: Long)(implicit ds: Datastore): Future[Map[String, RealNameInfo]] = {
    val query = ds.createQuery(classOf[UserInfo]).field("userId").equal(userId)
    Future {
      val ret = query.get
      if (ret != null) mapAsScalaMapConverter(ret.travellers).asScala.toMap else Map[String, RealNameInfo]()
    }
  }
}
