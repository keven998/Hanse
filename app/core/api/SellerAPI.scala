package core.api

import com.lvxingpai.model.geo.GeoEntity
import com.lvxingpai.model.marketplace.product.Commodity
import com.lvxingpai.model.marketplace.seller.Seller
import org.mongodb.morphia.Datastore

import scala.collection.JavaConversions._
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

  def getSeller(id: Long, fields: Seq[String])(implicit ds: Datastore): Future[Option[Seller]] = {
    Future {
      Option(ds.find(classOf[Seller], "sellerId", id).retrievedFields(true, fields: _*).get)
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

  def addSubLocalities(sellerId: Long, locality: Seq[GeoEntity])(implicit ds: Datastore): Future[Unit] = {
    Future {
      val statusQuery = ds.createQuery(classOf[Seller]) field "sellerId" equal sellerId
      val statusOps = ds.createUpdateOperations(classOf[Seller]).set("subLocalities", seqAsJavaList(locality))
      ds.update(statusQuery, statusOps)
    }
  }

  def getSubLocalities(sellerId: Long)(implicit ds: Datastore): Future[Option[Seq[GeoEntity]]] = {
    Future {
      val seller = ds.find(classOf[Seller], "sellerId", sellerId).retrievedFields(true, Seq("subLocalities"): _*).get()
      Option(seller.subLocalities)
    }
  }
}
