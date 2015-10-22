package com.lvxingpai.model.quora

import javax.validation.constraints.Min

import com.lvxingpai.model.account.UserInfo
import org.joda.time.Instant

import scala.beans.BeanProperty

/**
 * 问题
 *
 * Created by zephyre on 10/19/15.
 */
class Question extends AbstractQuoraEntry {

  /**
   * 几种常见的来源
   */
  object Source extends Enumeration {
    val Qyer, Ctrip, Baidu = Value
  }

  /**
   * 问题的来源
   */
  @BeanProperty
  var source: Option[Source.Value] = None

  /**
   * 问题的主题
   */
  @BeanProperty
  var topic: Seq[String] = Seq()

  /**
   * 问题的标签
   */
  @BeanProperty
  var tags: Seq[String] = Seq()

  /**
   * 问题被浏览的次数
   */
  @Min(value = 0)
  @BeanProperty
  var viewCnt: Int = 0

  /**
   * 问题被回答的次数
   */
  @Min(value = 0)
  @BeanProperty
  var answersCnt: Int = 0

  /**
   * 该问题的所有回答中，被赞的次数最高的数值
   */
  @Min(value = 0)
  @BeanProperty
  var maxVoteCnt: Int = 0
}

object Question {
  def apply(id: String, author: UserInfo, title: String, publishTime: Instant) = {
    val question = new Question
    question.id = id
    question.author = author
    question.title = title
    question.publishTime = publishTime
    question
  }
}
