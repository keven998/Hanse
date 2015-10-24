package core.utils

import com.fasterxml.jackson.databind.{ ObjectMapper, JsonNode }
import play.api.http.Status._
import play.api.mvc.{ Results, Result }

/**
 * Created by pengyt on 2015/10/23.
 */
object HanseResults {

  object RetCode extends Enumeration {

    val OK = Value(0)

    val INVALID_ARGUMENTS = Value(100, "Invalid arguments")
  }

  /**
   * 返回标准的HTTP响应
   * @return
   */
  def apply(status: Int = OK, retCode: RetCode.Value = RetCode.OK, data: Option[JsonNode] = None, errorMsg: Option[String] = None): Result = {
    // error信息的生成：如果retCode不为OK，且errorMsg为None，则生成默认的信息
    val errorWithDefault = errorMsg orElse {
      if (retCode != RetCode.OK)
        Some(retCode.toString)
      else
        None
    }
    val error = errorWithDefault map (v => s""",\"error\":$v""") getOrElse ""

    val mapper = new ObjectMapper()
    val node = mapper.createObjectNode()
    node put ("timestamp", System.currentTimeMillis()) put ("code", retCode.id)

    if (errorWithDefault nonEmpty)
      node put ("error", errorWithDefault.get)
    if (data nonEmpty)
      node set ("result", data.get)

    val contents = mapper.writeValueAsString(node)
    Results.Status(status)(contents).withHeaders("Content-Type" -> "application/json;charset=utf-8")
  }

  def ok(retCode: RetCode.Value = RetCode.OK, data: Option[JsonNode] = None, errorMsg: Option[String] = None): Result =
    HanseResults(OK, retCode, data, errorMsg)

  def unprocessable(retCode: RetCode.Value = RetCode.INVALID_ARGUMENTS, data: Option[JsonNode] = None,
    errorMsg: Option[String] = None): Result =
    HanseResults(UNPROCESSABLE_ENTITY, retCode, data, errorMsg)
}
