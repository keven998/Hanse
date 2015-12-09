package core.api

import com.lvxingpai.model.marketplace.order.Order
import core.db.MorphiaFactory
import org.bson.types.ObjectId
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 *
 * Created by topy on 2015/10/22.
 */
object TradeAPI {

  val ds = MorphiaFactory.datastore
  def getOrder(orderId: ObjectId): Future[Unit] = {
    Future {
      ds.find(classOf[Order], "orderId", orderId).asList()
    }
  }

}
