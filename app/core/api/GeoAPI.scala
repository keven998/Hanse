package core.api

import com.lvxingpai.model.geo.Country
import org.bson.types.ObjectId
import org.mongodb.morphia.Datastore

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
}
