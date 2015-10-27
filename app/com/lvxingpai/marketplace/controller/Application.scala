package com.lvxingpai.marketplace.controller

import play.api.mvc.{Action, Controller, Results}

import scala.concurrent.Future

/**
 * Created by zephyre on 10/27/15.
 */
class Application extends Controller {

  /**
   * 获得商品列表
   *
   * @return
   */
  def getCommodities = Action.async(request => {
    val sort = request.getQueryString("")

    Future(Results.Ok(""))
  })
}
