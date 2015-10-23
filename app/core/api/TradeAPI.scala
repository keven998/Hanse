package core.api

import core.db.MorphiaFactory
import core.model.trade.order.Order
import org.bson.types.ObjectId
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by topy on 2015/10/22.
 */
object TradeAPI {

  val ds = MorphiaFactory.datastore
  def getOrder(orderId: ObjectId): Future[Unit] = {
    Future {
      ds.find(classOf[Order], Order.FD_COMMODITY, orderId).asList()
    }
  }

}
