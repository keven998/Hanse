package core.misc

import com.lvxingpai.appconfig.{ AppConfig, EtcdConfBuilder, EtcdServiceBuilder }
import com.typesafe.config.{ Config, ConfigFactory }
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }
import scala.language.postfixOps

/**
 * Created by topy on 10/23/15.
 */
object Global {
  lazy val confFuture = {

    val playConf = Configuration(ConfigFactory.load())

    // 确定runlevel
    val runlevel = playConf.getString("runlevel").orNull

    import scala.concurrent.ExecutionContext.Implicits._
    val etcdFuture = Future.sequence[Config, Seq](runlevel match {
      case "production" =>
        Seq(
          EtcdConfBuilder().addKey("k2").build(),
          EtcdServiceBuilder().addKey("mongo-production", "mongo").addKey("smscenter").addKey("yunkai")
            .addKey("redis-main", "redis").addKey("hedy").build()
        )
      case "dev" =>
        Seq(
          EtcdConfBuilder().addKey("k2-dev", "k2").build(),
          EtcdServiceBuilder().addKey("mongo-dev", "mongo").addKey("smscenter").addKey("yunkai-dev", "yunkai")
            .addKey("redis-main", "redis").addKey("hedy-dev", "hedy").build()
        )
      case _ => Seq()
    }) map (v => v map (Configuration(_)))

    etcdFuture map (v => {
      (v :+ playConf) reduce ((c1, c2) => c1 ++ c2)
    })
  }
  lazy val conf: Configuration = Await.result(confFuture, 100 seconds)
}