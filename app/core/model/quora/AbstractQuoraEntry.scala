package core.model.quora

import javax.validation.constraints.NotNull

import core.model.BasicEntity
import core.model.account.UserInfo
import org.hibernate.validator.constraints.NotBlank

import scala.beans.BeanProperty

/**
 * 问答数据的基础类
 *
 * Created by zephyre on 10/19/15.
 */
abstract class AbstractQuoraEntry extends BasicEntity {

  /**
   * 作者信息
   */
  @NotNull
  @BeanProperty
  var author: UserInfo = null

  /**
   * 发表时间戳
   */
  @NotNull
  @BeanProperty
  var publishTime: Long = 0

  /**
   * 标题
   */
  @NotBlank
  @BeanProperty
  var title: String = null

  /**
   * 具体描述
   */
  @NotNull
  @BeanProperty
  var contents: String = null
}
