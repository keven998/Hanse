package core.inject

import javax.inject.{ Inject, Provider }

import core.api.{ ElasticsearchEngine, SearchEngine }
import play.api.inject.{ Binding, BindingKey, Injector, Module }
import play.api.{ Configuration, Environment }

/**
 * Created by zephyre on 1/30/16.
 */
class EngineModule extends Module {
  override def bindings(environment: Environment, configuration: Configuration): Seq[Binding[_]] = {
    Seq(bind[SearchEngine] to new EngineProvider)
  }
}

class EngineProvider extends Provider[SearchEngine] {
  @Inject private var injector: Injector = _

  lazy val get: SearchEngine = {
    val confKey = BindingKey(classOf[Configuration]) qualifiedWith "etcdService"
    val services = injector instanceOf confKey

    val es = services getConfig "services.elasticsearch" getOrElse Configuration.empty
    val subKeys = es.subKeys.toSeq
    // 服务器地址设置:  [ {host: "xxx", port: 2379} ]
    val addresses = subKeys map (serverKey => {
      for {
        host <- es getString s"$serverKey.host"
        port <- es getInt s"$serverKey.port"
      } yield {
        s"$host:$port"
      }
    }) filter (_.nonEmpty) map (_.get)
    val uri = s"elasticsearch://${addresses mkString ","}"
    new ElasticsearchEngine(uri)
  }
}
