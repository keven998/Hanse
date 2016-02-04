package core.misc

import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper }
import play.api.http.Status._
import play.api.mvc.{ Result, Results }

import scala.language.postfixOps

/**
 * Created by topy on 10/23/15.
 */
object HanseResult {

  object RetCode extends Enumeration {
    val OK = Value(0)
    val INVALID_ARGUMENTS = Value(100, "Invalid arguments")
    val FORBIDDEN = Value(403, "Forbidden")
    val NOT_FOUND = Value(404, "Resource not found")
    val ALIPAY_REFUND = Value(901, "Ali pay refund need manual operation")
    val UNKNOWN = Value(999, "Unknown error")
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

  // 正常
  def ok(retCode: RetCode.Value = RetCode.OK, data: Option[JsonNode] = None, errorMsg: Option[String] = None): Result =
    HanseResult(OK, retCode, data, errorMsg)

  def notFound(message: Option[String] = None): Result = HanseResult(NOT_FOUND, RetCode.NOT_FOUND, errorMsg = message)

  // 请求中的参数有问题
  def unprocessable(retCode: RetCode.Value = RetCode.INVALID_ARGUMENTS, data: Option[JsonNode] = None,
    errorMsg: Option[String] = None): Result =
    HanseResult(UNPROCESSABLE_ENTITY, retCode, data, errorMsg)

  // 权限错误
  def forbidden(data: Option[JsonNode] = None, errorMsg: Option[String] = None): Result =
    HanseResult(FORBIDDEN, RetCode.FORBIDDEN, data, errorMsg)

  def unprocessableWithMsg(errorMsg: Option[String]): Result =
    HanseResult(UNPROCESSABLE_ENTITY, RetCode.INVALID_ARGUMENTS, None, errorMsg)

  def conflict(errorMsg: Option[String] = None): Result = HanseResult(CONFLICT, RetCode.UNKNOWN, None, errorMsg)
}