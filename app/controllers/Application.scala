package controllers

import javax.inject.Inject

import play.api.Play
import play.api.Play.current
import play.api.cache.CacheApi
import play.api.inject.BindingKey
import play.api.mvc._
import play.cache.{ NamedCache, NamedCacheImpl }

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application @Inject() (@NamedCache("db-cache") cache: CacheApi) extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  // 使用示例：curl -i -XPOST localhost:9000/redis/keys -d "key=test&value=testredis"
  def setRedisKey() = Action.async(request => {
    (for {
      postData <- request.body.asFormUrlEncoded
      key <- postData get "key" map (_ mkString "|")
      value <- postData get "value" map (_ mkString "|")
    } yield {
      cache.set(key, value)
      Future(Results.Ok(s"Redis OK: $key => $value"))
    }) getOrElse Future(Results.UnprocessableEntity("Invalid request"))
  })

  // 使用示例：curl -i localhost:9000/redis/keys/test
  def getRedisData(key: String) = Action.async({
    // 注意：这里没有使用系统自动注入的cache，而是我们自己通过依赖注入容器，获得cache2这个实例。
    val cache2 = Play.application.injector.instanceOf(
      new BindingKey(classOf[CacheApi]) qualifiedWith new NamedCacheImpl("db-cache"))

    // 事实上，由于都是对应于db-cache，所以cache和cache2实际上是等价的——但是获取渠道不同
    assert(cache == cache2)

    val value = cache2.get[String](key)
    Future(Results.Ok(s"Redis ready at $key: $value"))
  })
}
