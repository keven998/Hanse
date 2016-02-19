package core.search

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.QueryDefinition

/**
 * Created by topy on 2016/2/18.
 */
class CategoryFilter(category: String) extends ElasticsearchFilter {

  override def getQueryDefinition(): QueryDefinition = {
    termQuery("category", category)
  }
}
