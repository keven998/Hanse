package core.inject

import javax.inject.{ Inject, Provider, Singleton }

import com.lvxingpai.etcd.EtcdStoreModule
import play.api.inject._
import play.api.{ Configuration, Environment }

/**
 * Created by zephyre on 11/6/15.
 */
class DefaultConfigurationModule extends Module {

  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    val confKey = bind[Configuration] qualifiedWith "etcdConf"
    val serviceKey = bind[Configuration] qualifiedWith "etcdService"
    val binding = bind[Configuration] qualifiedWith "default" to
      new DefaultConfigurationProvider(configuration, confKey, serviceKey)

    val module = new EtcdStoreModule
    module.bindings(environment, configuration) :+ binding
  }
}

@Singleton
class DefaultConfigurationProvider(config: Configuration, confKey: BindingKey[Configuration], serviceKey: BindingKey[Configuration])
    extends Provider[Configuration] {

  @Inject private var injector: Injector = _

  lazy val get: Configuration = {
    val conf = injector instanceOf confKey
    val services = injector instanceOf serviceKey

    // MongoDB的服务器连接设置
    val mongoService = services.getConfig("services.mongo") getOrElse Configuration.empty
    // MongoDB的入口可能很多, 我们取第一个
    val subKeys = mongoService.subKeys.toSeq

    // 服务器地址设置:  [ {host: "xxx", port: 2379} ]
    val serverConf = subKeys map (serverKey => {
      val host = mongoService.getString(s"$serverKey.host").get
      val port = mongoService.getInt(s"$serverKey.port").get
      Map("host" -> host, "port" -> port)
    })

    // 处理conf中的mongo部分
    // hanse.mongo中, 可能存在多个key, 对应多个数据库的设置, 比如
    val mongoKeys = conf.getConfig("hanse.mongo") map (_.subKeys.toSeq) getOrElse Seq()
    // 每个数据库都对应自己的Configuration
    val configList = mongoKeys map (key => {
      val mongoConfig = conf.getConfig(s"hanse.mongo.$key").get
      // 登录设置
      val authConfig = (for {
        user <- mongoConfig getString "user"
        password <- mongoConfig getString "password"
      } yield {
        Configuration(s"morphiaPlay.mongo.$key.user" -> user, s"morphiaPlay.mongo.$key.password" -> password)
      }) getOrElse Configuration.empty
      // 数据库设置
      val dbName = mongoConfig getString "database" getOrElse "local"
      // 服务器地址设置

      val connConfig = Configuration(s"morphiaPlay.mongo.$key.servers" -> serverConf, s"morphiaPlay.mongo.$key.database" -> dbName)
      authConfig ++ connConfig
    })

    val mongoConfig = configList reduce (_ ++ _)

    // 合并
    config ++ conf ++ services ++ mongoConfig
  }
}
