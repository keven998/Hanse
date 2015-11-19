package core.api

import core.db.MorphiaFactory
import core.model.trade.product.Commodity
import core.model.trade.saler.Saler

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

  // 先写在这，需要调用yunkai
  def getSaler(salerId: Long): Saler = {
    val saler = new Saler
    saler.userId = 100053
    saler.nickname = "逍遥"
    saler.avatar = "shuaishuaishuai"
    saler
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

    val saler = getSaler(salerId)
    val cmyInfo = Commodity(saler, title, detail, price)
    ds.save[Commodity](cmyInfo)
    Future {
      cmyInfo
    }
  }
}
