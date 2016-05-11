package core.search

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.QueryDefinition

/**
 * Created by topy on 2016/2/18.
 */
class CommodityTypeFilter(cType: String) extends ElasticsearchFilter {
  override val queryDefinition: QueryDefinition = termQuery("commodityType", cType)
}

object CommodityTypeFilter {
  def apply(cType: String) = new CommodityTypeFilter(cType)
}

