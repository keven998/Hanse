package core.api

import com.lvxingpai.model.marketplace.product.Commodity
import com.lvxingpai.model.marketplace.seller.Seller
import com.lvxingpai.model.misc.ImageItem
import org.mongodb.morphia.Datastore

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by topy on 2015/11/3.
 */
object SellerAPI {

  def getSeller(id: Long)(implicit ds: Datastore): Future[Option[Seller]] = {
    Future {
      Option(ds.find(classOf[Seller], "sellerId", id).get)
    }
  }

  def saveSeller(s: Seller)(implicit ds: Datastore) = {
    Future {
      ds.save[Seller](s)
    }
  }

  def setSeller()(implicit ds: Datastore) = {
    Future {
      val img = new ImageItem()
      img.key = "13e187722cbf20631ec6eb049d05a20c"
      img.height = 447
      img.width = 800
      val update = ds.createUpdateOperations(classOf[Seller]).set("cover", img)
      ds.update(ds.find(classOf[Seller], "sellerId", 100), update)
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
