package core.db

import com.lvxingpai.model.trade.order.{ Order, Prepay }
import com.mongodb._
import core.misc.Global
import org.mongodb.morphia.Morphia

import scala.collection.JavaConversions._
import scala.language.postfixOps

/**
 * Created by zephyre on 5/4/15.
 */
object MorphiaFactory {

  lazy val morphia = {
    val m = new Morphia()
    m.map(classOf[Order], classOf[Prepay])
    m
  }

  lazy val client = {
    val conf = Global.conf

    val mongoBackends = conf.getConfig("backends.mongo").get
    val services = mongoBackends.subKeys.toSeq map (mongoBackends.getConfig(_).get)

    val serverAddress = services map (c => {
      new ServerAddress(c.getString("host").get, c.getInt("port").get)
    })

    val mongoConfig = conf.getConfig("k2.mongo").get
    val dbName = mongoConfig.getString("db").get
    val credential = for {
      user <- mongoConfig.getString("user")
      password <- mongoConfig.getString("password")
    } yield {
      MongoCredential.createScramSha1Credential(user, dbName, password.toCharArray)
    }

    val options = new MongoClientOptions.Builder()
      //连接超时
      .connectTimeout(60000)
      //IO超时
      .socketTimeout(10000)
      //与数据库能够建立的最大连接数
      .connectionsPerHost(50)
      //每个连接可以有多少线程排队等待
      .threadsAllowedToBlockForConnectionMultiplier(50)
      .build()

    if (credential nonEmpty)
      new MongoClient(serverAddress, Seq(credential.get), options)
    else
      new MongoClient(serverAddress, options)
  }

  lazy val datastore = {
    val dbName = Global.conf.getString("k2.mongo.db").get
    val ds = morphia.createDatastore(client, dbName)
    ds.ensureIndexes()
    ds.ensureCaps()
    ds
  }
}