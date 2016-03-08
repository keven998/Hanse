package core.search

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.QueryDefinition

/**
 * Created by topy on 2016/2/18.
 */
class SellerFilter(sellerId: Long) extends ElasticsearchFilter {
  override val queryDefinition: QueryDefinition = termQuery("seller.sellerId", sellerId)
}

object SellerFilter {
  def apply(sellerId: Long) = new SellerFilter(sellerId)
}