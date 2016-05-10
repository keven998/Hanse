package core.model.misc

import java.util.{ List => JList }

import com.lvxingpai.model.marketplace.seller.Seller
import org.bson.types.ObjectId
import org.mongodb.morphia.annotations.{ Entity, Id }

import scala.beans.BeanProperty

/**
 * Created by pengyt on 2015/11/13.
 */
@Entity
class GeoCommodity {

  @Id
  var id: ObjectId = _

  /**
   * 城市或国家的ID
   */
  @BeanProperty
  var geoId: ObjectId = _

  /**
   * 商家列表
   *
   */
  var sellers: JList[Seller] = _
}
