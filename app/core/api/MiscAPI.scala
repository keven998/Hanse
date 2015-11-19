package core.api

import com.lvxingpai.model.marketplace.product.Commodity
import core.db.MorphiaFactory
import core.model.misc.{ RecommendCategory, Column, TopicCommodity }

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
   * @return 运营位列表
   */
  def getColumns(): Future[Map[String, Seq[Column]]] = {
    val query = ds.createQuery(classOf[Column])
    Future {
      if (query != null || query.isEmpty) {
        query.asList().groupBy(_.columnType) map (columnMap => columnMap._1 -> columnMap._2.toSeq)
      } else
        Map()
    }
  }

  /**
   * 根据话题类型查找商品列表
   * @return 商品列表
   */
  def getCommoditiesByTopic(topicType: String): Future[Seq[Commodity]] = {
    val query = ds.createQuery(classOf[TopicCommodity]).field("topicType").equal(topicType)

    if (query != null || query.isEmpty) {
      CommodityAPINew.getCommoditiesByIdList(query.get.commoditieIds)
    } else
      Future { Seq() }
  }

  /**
   * 根据话题类型查找商品列表
   * @return 商品列表
   */
  def getRecommendCommodities(categories: Seq[String]): Future[Map[String, Seq[Commodity]]] = {
    val query = ds.createQuery(classOf[TopicCommodity]).field("topicType").in(categories)

    Future {
      val topicEntries = query.asList().toSeq

      val topicIdsMap = Map(topicEntries map (v => {
        v.topicType -> v
      }): _*)

      // 获得所有相关的商品id
      val allIds = topicEntries.foldLeft(Seq[Long]())((l, tc) => {
        l ++ tc.commoditieIds
      })

      val futureAllCommodities = CommodityAPINew.getCommoditiesByIdList(allIds)

      futureAllCommodities map (allCommodities => {
        val idCommodityMap = Map(allCommodities map (commodity => {
          commodity.commodityId -> commodity
        }): _*)

        Map(categories map (topic => {
          topic -> (topicIdsMap(topic).commoditieIds map (id => {
            idCommodityMap(id)
          }))
        }): _*)

      })
    } flatMap (topicCommoditiesMap => topicCommoditiesMap)
    //val ret = query.asList().groupBy(_.topicType)
  }

  /**
   * 取得推荐商品分类
   * @return 推荐分类
   */
  def getRecommendCategories(): Future[Seq[String]] = {
    val query = ds.createQuery(classOf[RecommendCategory])
    Future {
      query.get().categories.toSeq
    }
  }
}
