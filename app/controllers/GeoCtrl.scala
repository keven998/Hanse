package controllers

import javax.inject.{ Named, Inject }

import com.lvxingpai.inject.morphia.MorphiaMap
import com.lvxingpai.model.geo.Country
import core.api.GeoAPI
import org.bson.types.ObjectId
import play.api.Configuration
import play.api.mvc.Controller

import scala.concurrent.Future

/**
 * Created by pengyt on 2015/11/20.
 */
class GeoCtrl @Inject() (@Named("default") configuration: Configuration, datastore: MorphiaMap) extends Controller {
  implicit lazy val ds = datastore.map.get("k2").get
  def getCountryById(id: ObjectId): Future[Country] = {
    GeoAPI.getCountryById(id)
  }
}
