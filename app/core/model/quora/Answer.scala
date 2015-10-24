package core.model.quora

import javax.validation.constraints.{ Min, NotNull }

import core.model.account.UserInfo
import org.bson.types.ObjectId

import scala.beans.BeanProperty

/**
 * 回答
 *
 * Created by zephyre on 10/19/15.
 */
class Answer extends AbstractQuoraEntry {
  /**
   * 对应的问题
   */
  @NotNull
  @BeanProperty
  var question: Question = null

  /**
   * 被赞的次数
   */
  @Min(value = 0)
  @BeanProperty
  var voteCnt: Int = 0

  /**
   * 该回答是否被采纳
   */
  @BeanProperty
  var accepted: Boolean = false
}

object Answer {
  def apply(id: ObjectId, question: Question, author: UserInfo, title: String, publishTime: Long) = {
    val answer = new Answer
    answer.id = id
    answer.question = question
    answer.author = author
    answer.title = title
    answer.publishTime = publishTime
    answer
  }
}