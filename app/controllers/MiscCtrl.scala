package controllers

import com.fasterxml.jackson.databind.JsonNode
import core.api.MiscAPI
import core.formatter.misc.ColumnGroupFormatter
import core.misc.HanseResult
import core.model.misc.ColumnGroup
import play.api.mvc.{Action, Controller}

import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by pengyt on 2015/11/13.
 */
class MiscCtrl  extends Controller {

  /**
   * 首页专题
   * @return
   */
  def getColumns() = Action.async(
    request => {
      val columnGroupMapper = new ColumnGroupFormatter().objectMapper
      val columnSlideGroup = new ColumnGroup
      val columnSpecialGroup = new ColumnGroup
      for {
        columnsSlide <- MiscAPI.getColumns("slide")
        columnsSpecial <- MiscAPI.getColumns("special")
      } yield {
        columnSlideGroup.columnType = "slide"
        columnSlideGroup.columns = columnsSlide
        columnSpecialGroup.columnType = "special"
        columnSpecialGroup.columns = columnsSpecial
        val node = columnGroupMapper.valueToTree[JsonNode](Seq(columnSlideGroup, columnSpecialGroup))
        HanseResult(data = Some(node))
      }
  })


}
