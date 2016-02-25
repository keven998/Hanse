package core.search

import com.sksamuel.elastic4s.ElasticDsl._
import com.sksamuel.elastic4s.TermQueryDefinition

/**
 * Created by topy on 2016/2/18.
 */
class LocalityFilter(locId: String) extends ElasticsearchFilter {
  override val queryDefinition: TermQueryDefinition = termQuery("locality._id", locId)
}

object LocalityFilter {
  def apply(locId: String) = new LocalityFilter(locId)
}
