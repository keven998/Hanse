package core.api

import core.db.MorphiaFactory
import core.model.trade.order.Order

import scala.concurrent.Future
import org.bson.types.ObjectId
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Created by topy on 2015/10/22.
 */
object OrderAPI {

  val ds = MorphiaFactory.datastore
  def getOrder(orderId: ObjectId): Future[Unit] = {
    Future {
      ds.find(classOf[Order], Order.FD_COMMODITY, orderId).asList()
    }
  }

  /**
   * 创建订单
   * @param cmyId 商品Id
   * @param qty 商品数量
   * @return
   */
  def addOrder(cmyId: String, qty: Int): Future[Order] = {
    val futureCmy = CommodityAPI.getCommodityById(cmyId)
    for {
      cmy <- futureCmy
    } yield {
      val order = Order(cmy, qty)
      ds.save[Order](order)
      order
    }
  }
}
