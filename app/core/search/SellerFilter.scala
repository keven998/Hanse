package core.search

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.QueryDefinition

/**
 * Created by topy on 2016/2/18.
 */
class SellerFilter(sellerId: Long) extends ElasticsearchFilter {

  override def getQueryDefinition(): QueryDefinition = {
    termQuery("seller.sellerId", sellerId)
  }
}
