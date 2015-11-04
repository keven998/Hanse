package core.api

import com.lvxingpai.model.marketplace.seller.Seller
import core.model.trade.product.Commodity
import org.mongodb.morphia.Datastore

import scala.concurrent.Future
import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global


/**
 * Created by topy on 2015/11/3.
 */
object SellerAPI {

  def getSeller(id: Long)(implicit ds: Datastore): Future[Seller] = {
    Future {
      ds.find(classOf[Seller], "id", id).get
    }
  }

  def getCommoditiesBySeller(sId: Long)(implicit ds: Datastore): Future[Seq[Commodity]] = {
    val query = ds.createQuery(classOf[Commodity]).field("seller.id").equal(sId)
      .retrievedFields(true, Seq("plans.pricing", "plans.marketPrice", ""): _*)
    Future {
      query.asList()
    }
  }

}
