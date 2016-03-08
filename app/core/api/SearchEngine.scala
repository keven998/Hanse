package core.api

import com.lvxingpai.model.marketplace.product.Commodity
import core.search.ElasticsearchFilter

import scala.concurrent.Future

/**
 * Created by zephyre on 1/30/16.
 */
trait SearchEngine {

  /**
   * 搜索商品: 综合排序
   * @param query 搜索的关键词
   */
  def overallCommodities(query: Option[String] = None, filters: Seq[ElasticsearchFilter], sortBy: String, sort: String, start: Int, count: Int): Future[Seq[Commodity]]

}
