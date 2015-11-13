package core.api

import core.db.MorphiaFactory
import core.model.misc.{ Column, TopicCommodity }

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by pengyt on 2015/11/13.
 */
object MiscAPI {

  val ds = MorphiaFactory.datastore

  /**
   * 取得运营位列表
   * @param columnType 运营位类型
   * @return 运营位列表
   */
  def getColumns(columnType: String): Future[Seq[Column]] = {
    val query = ds.createQuery(classOf[Column]).field("columnType").equal(columnType)
    Future {
      if (query != null || query.isEmpty)
        query.asList().toSeq
      else
        Seq()
    }
  }

  /**
   * 根据话题类型查找商品列表
   * @param topicType 话题类型
   * @return 商品列表
   */
  def getSpecialCommodities(topicType: String): Future[Seq[TopicCommodity]] = {
    val query = ds.createQuery(classOf[TopicCommodity]).field("topicType").equal(topicType)
    Future {
      if (query != null || query.isEmpty)
        query.asList().toSeq
      else
        Seq()
    }
  }

  /**
   * 根据话题类型查找商品列表
   * @param recommendType 话题类型
   * @return 商品列表
   */
  def getRecommendCommodities(recommendType: String): Future[Seq[TopicCommodity]] = {
    val query = ds.createQuery(classOf[TopicCommodity]).field("recommendType").equal(recommendType)
    Future {
      if (query != null || query.isEmpty)
        query.asList().toSeq
      else
        Seq()
    }
  }
}
