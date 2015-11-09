package core.api

import com.lvxingpai.model.marketplace.seller.Seller
import core.model.trade.product.Commodity
import org.mongodb.morphia.Datastore

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by topy on 2015/11/3.
 */
object SellerAPI {

  def getSeller(id: Long)(implicit ds: Datastore): Future[Seller] = {
    Future {
      //ds.createQuery(classOf[Seller]).field("id").equal(id).get
      ds.find(classOf[Seller], "_id", id).get
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
