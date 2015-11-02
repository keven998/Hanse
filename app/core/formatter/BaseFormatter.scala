package core.formatter

import com.fasterxml.jackson.databind.{ JsonNode, ObjectMapper }

/**
 * Created by pengyt on 2015/8/28.
 */
trait BaseFormatter {
  protected val objectMapper: ObjectMapper

  def formatJsonNode(obj: AnyRef): JsonNode = objectMapper.valueToTree(obj)

  def formatString(obj: AnyRef): String = objectMapper.writeValueAsString(obj)
}