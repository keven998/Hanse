package core.search

import com.sksamuel.elastic4s.ElasticDsl._

/**
 * Created by zephyre on 2/25/16.
 */
class CommodityStatusFilter(status: String) extends ElasticsearchFilter {
  override val queryDefinition = termQuery("status", status)
}

object CommodityStatusFilter {
  def apply(status: String = "pub") = new CommodityStatusFilter(status)
}