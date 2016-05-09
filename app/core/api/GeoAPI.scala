package core.api

import com.lvxingpai.model.geo.{ Country, Locality }
import org.bson.types.ObjectId
import org.mongodb.morphia.Datastore

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by pengyt on 2015/11/20.
 */
object GeoAPI {
  /**
   * 根据国家id取得国家信息
   */
  def getCountryById(id: ObjectId)(implicit ds: Datastore): Future[Country] = {
    val query = ds.createQuery(classOf[Country]).field("id").equal(id)
    Future {
      query.get
    }
  }

  def getLocalityById(id: ObjectId, fields: Option[Seq[String]])(implicit ds: Datastore): Future[Locality] = {
    val query = ds.createQuery(classOf[Locality]).field("id").equal(id)
    if (fields.nonEmpty)
      query.retrievedFields(true, fields.get: _*)
    Future {
      query.get
    }
  }
}
