package core.service

import com.fasterxml.jackson.databind.ObjectMapper

import play.api.Play.current
import play.api.libs.ws.WS

/**
 * Created by topy on 2016/1/11.
 */
object MQService {

  def sendMessage(order: String, name: String) = {
    val node = new ObjectMapper().createObjectNode()
    node.put("task", name)
    node.put("kwargs", order)
    val body = new ObjectMapper().createObjectNode()
    body.set("order", node)
    val wsFuture = WS.url("http://192.168.100.105:8423/tasks")
      .withHeaders("Content-Type" -> "application/json; charset=utf-8")
      .withRequestTimeout(30000)
      .post(body)
    wsFuture
  }
}
