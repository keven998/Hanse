package core.api

import com.lvxingpai.model.trade.order.Order
import com.twitter.util.{ Future, FuturePool }
import org.bson.types.ObjectId
import org.mongodb.morphia.Datastore

/**
 * Created by topy on 2015/10/22.
 */
object TradeAPI {

  def getOrder(orderId: ObjectId)(implicit ds: Datastore, futurePool: FuturePool): Future[Unit] = {
    Future {
      ds.find(classOf[Order], Order.FD_COMMODITY, orderId).asList()
    }
  }

}
