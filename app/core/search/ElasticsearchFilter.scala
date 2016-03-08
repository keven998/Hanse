package core.search

import com.sksamuel.elastic4s.QueryDefinition

/**
 * Created by topy on 2016/2/18.
 */
trait ElasticsearchFilter {
  val queryDefinition: QueryDefinition
}

