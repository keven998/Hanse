package core.search

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.QueryDefinition

/**
 * Created by topy on 2016/2/18.
 */
class CategoryFilter(category: String) extends ElasticsearchFilter {
  override val queryDefinition: QueryDefinition = termQuery("category", category)
}

object CategoryFilter {
  def apply(category: String) = new CategoryFilter(category)
}
