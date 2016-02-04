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
    val config = injector instanceOf (BindingKey(classOf[Configuration]) qualifiedWith "default")

    // Elasticsearch服务器的地址: [ {host: "xxx", port: 2379} ]
    val es = config getConfig "services.elasticsearch" getOrElse Configuration.empty
    val subKeys = es.subKeys.toSeq
    val addresses = subKeys map (serverKey => {
      for {
        host <- es getString s"$serverKey.host"
        port <- es getInt s"$serverKey.port"
      } yield {
        s"$host:$port"
      }
    }) filter (_.nonEmpty) map (_.get)
    val uri = s"elasticsearch://${addresses mkString ","}"

    // Elasticsearch的index信息
    val indexName = (config getString "hanse.elasticsearch.index").get

    new ElasticsearchEngine(ElasticsearchEngine.Settings(uri, indexName))
  }
}
