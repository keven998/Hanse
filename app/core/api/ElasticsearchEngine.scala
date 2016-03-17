package core.api

import com.lvxingpai.model.marketplace.product.Commodity
import com.lvxingpai.model.misc.ImageItem
import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.{ ElasticClient, HitAs, RichSearchHit }
import core.search.ElasticsearchFilter
import org.elasticsearch.common.lucene.search.function.FieldValueFactorFunction
import org.elasticsearch.search.sort.SortOrder

import scala.concurrent.Future

/**
 * Created by zephyre on 1/30/16.
 */
class ElasticsearchEngine(settings: ElasticsearchEngine.Settings) extends SearchEngine {

  lazy val client = ElasticClient.transport(settings.uri)

  implicit object CommodityHitAs extends HitAs[Commodity] {
    def asImageItem(source: Map[String, Any]): Option[ImageItem] = {
      None
    }

    override def as(hit: RichSearchHit): Commodity = {
      val source = hit.sourceAsMap

      val commodity = new Commodity
      commodity.commodityId = source("commodityId").asInstanceOf[Number].longValue()
      commodity.title = source.getOrElse("title", "").toString
      commodity.rating = source.getOrElse("rating", 0).asInstanceOf[Number].doubleValue()
      commodity.salesVolume = source.getOrElse("salesVolume", 0).asInstanceOf[Number].intValue()
      commodity.marketPrice = source.getOrElse("marketPrice", 0).asInstanceOf[Number].intValue()
      commodity.price = source.getOrElse("price", 0).asInstanceOf[Number].intValue()
      commodity.status = source.getOrElse("status", "").toString

      commodity
    }
  }

  /**
   * 搜索商品: 综合排序
   * @param q 搜索的关键词
   */
  override def overallCommodities(q: Option[String] = None, filters: Seq[ElasticsearchFilter], sortBy: String, sort: String, startIndex: Int, count: Int): Future[Seq[Commodity]] = {
    val s = {
      val head = search in settings.index / "commodity"

      // 定义搜索条件
      val queryClause = q map (v => should(
        matchQuery("title", v) boost 3,
        matchQuery("desc.summary", v)
      ) minimumShouldMatch 1) getOrElse must(matchAllQuery)
      // 筛选条件
      val filterClause = filter(Option(filters) getOrElse Seq() map (_.queryDefinition))
      // 综合
      val boolCondition = queryClause filter filterClause

      // 和rating相关的function score
      val defaultRating = 0.7
      val ratingFunction = fieldFactorScore("rating") missing defaultRating modifier FieldValueFactorFunction.Modifier.SQUARE
      // 和salesVolume相关的function score
      val salesFunction = fieldFactorScore("salesVolume") missing 1 modifier FieldValueFactorFunction.Modifier.LOG2P
      // 和weightBoost相关的function score
      val weightBoostFunction = fieldFactorScore("weightBoost") missing 1

      // 把搜索条件组织成查询语句
      val queryCondition = head start startIndex limit count query {
        functionScoreQuery(
          boolCondition
        ) scorers (ratingFunction, salesFunction, weightBoostFunction)
      }

      // 定义排序条件
      val sortCondition = fieldSort(sortBy).order(if (sort.equals("asc")) SortOrder.ASC else SortOrder.DESC)

      // 将查询语句与排序条件拼接
      if (sortBy.equals("relevance")) queryCondition else queryCondition sort sortCondition
    }
    import scala.concurrent.ExecutionContext.Implicits.global

    client.execute(s) map (_.as[Commodity])
  }
}

object ElasticsearchEngine {

  /**
   * @param uri elasticsearch的uri
   * @param index index的名称
   */
  case class Settings(uri: String, index: String)

}
