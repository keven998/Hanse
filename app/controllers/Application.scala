package controllers

import javax.inject.{ Inject, Named }
import com.lvxingpai.inject.morphia.MorphiaMap
import play.api._
import play.api.mvc._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class Application @Inject() (@Named("default") configuration: Configuration, datastore: MorphiaMap) extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def ping = {
    val c = configuration
    val d = datastore
    Logger.logger.info(s"The injected configuration is {${c.subKeys mkString ", "}}")
    Logger.logger.info(s"The injected datastores are ${d.map.keys mkString ", "}")
    Action.async(Future(Results.Ok))
  }
}

