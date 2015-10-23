package core.model.misc

import javax.validation.constraints.Min

import core.model.BasicEntity
import org.joda.time.Instant

import scala.beans.BeanProperty

/**
 * 举报
 * Created by pengyt on 2015/10/21.
 */
class TipOff extends BasicEntity {

  /**
   * 举报内容
   */
  @BeanProperty
  var body: String = null

  /**
   * 举报对象
   */
  @Min(value = 1)
  @BeanProperty
  var targetUserId: Long = 0

  /**
   * 提交举报的人
   */
  @Min(value = 1)
  @BeanProperty
  var offerUserId: Long = 0

  /**
   * 创建时间
   */
  var cTime: Long = 0

}
