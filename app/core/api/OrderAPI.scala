package core.api

import com.lvxingpai.model.trade.order.Order
import scala.concurrent.Future
import org.bson.types.ObjectId
import org.mongodb.morphia.Datastore
import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Created by topy on 2015/10/22.
 */
object OrderAPI {

  def getOrder(orderId: ObjectId)(implicit ds: Datastore): Future[Unit] = {
    Future {
      ds.find(classOf[Order], Order.FD_COMMODITY, orderId).asList()
    }
  }

}
