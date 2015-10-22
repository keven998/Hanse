package controllers

import javax.inject._

import play.api.mvc.{Action, Controller, Result}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by topy on 2015/10/22.
 */
@Singleton
class TradeCtrl extends Controller {

  def createOrder() = Action.async(
    request => {
      val res: Result = null
      Future {
        res
      }
    })
}
