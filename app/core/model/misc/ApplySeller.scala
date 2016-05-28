package core.model.misc

import org.bson.types.ObjectId
import org.mongodb.morphia.annotations.{ Entity, Id }

import scala.beans.BeanProperty

/**
 * Created by topy on 2016/5/28.
 */
@Entity
class ApplySeller {

  @Id
  @BeanProperty
  var id: ObjectId = _

  @BeanProperty
  var name: String = _

  @BeanProperty
  var tel: String = _

  @BeanProperty
  var province: String = _

  @BeanProperty
  var city: String = _

  @BeanProperty
  var travel: String = _

  @BeanProperty
  var license: String = _

  @BeanProperty
  var email: String = _

  @BeanProperty
  var memo: String = _

}
