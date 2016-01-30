package core.api

import com.lvxingpai.model.marketplace.product.Commodity

import scala.concurrent.Future

/**
 * Created by zephyre on 1/30/16.
 */
trait SearchEngine {

  /**
   * 搜索商品: 综合排序
   * @param query 搜索的关键词
   */
  def overallCommodities(query: Option[String] = None): Future[Seq[Commodity]]
}
