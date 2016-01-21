package core.api

import com.lvxingpai.model.marketplace.product.Commodity
import com.lvxingpai.model.marketplace.seller.Seller
import org.mongodb.morphia.Datastore

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 *
 * Created by topy on 2015/11/3.
 */
object SellerAPI {

  def getSeller(id: Long)(implicit ds: Datastore): Future[Option[Seller]] = {
    Future {
      Option(ds.find(classOf[Seller], "sellerId", id).get)
    }
  }

  def getSeller(commodity: Option[Commodity])(implicit ds: Datastore): Future[Option[Seller]] = {
    Future {
      commodity match {
        case None => None
        case c if c.get.seller == null => None
        case _ => Option(ds.find(classOf[Seller], "sellerId", commodity.get.seller.sellerId).get)
      }
    }
  }
}
