package core.search

import com.sksamuel.elastic4s.ElasticDsl._

/**
 * Created by topy on 2016/3/9.
 */
class CommodityStatusOptFilter(status: String) extends ElasticsearchFilter {
  override val queryDefinition = termQuery("status", status)
}

object CommodityStatusOptFilter {
  def apply(status: String) = new CommodityStatusOptFilter(status)
}