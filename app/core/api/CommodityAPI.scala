package core.api

import com.lvxingpai.model.marketplace.product.Commodity
import com.lvxingpai.model.marketplace.seller.Seller
import core.db.MorphiaFactory

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by pengyt on 2015/10/23.
 */
object CommodityAPI {

  val ds = MorphiaFactory.datastore
  /**
   * 根据商品Id取得商品信息
   * @param cmyId
   * @return
   */
  def getCommodityById(cmyId: Long): Future[Commodity] = {
    val query = ds.createQuery(classOf[Commodity]).field("commodityId").equal(cmyId)
    Future {
      query.get
    }
  }

  /**
   * 添加商品
   * @param salerId
   * @param title
   * @param detail
   * @param price
   * @return
   */
  def addCommodity(salerId: Long, title: String, detail: String, price: Float, fields: Map[String, AnyRef] = Map()): Future[Commodity] = {

    // TODO
    val saler = new Seller
    val cmyInfo = new Commodity
    ds.save[Commodity](cmyInfo)
    Future {
      cmyInfo
    }
  }
}
