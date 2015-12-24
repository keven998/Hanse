package core.model.misc

import java.util.{ List => JList }

import org.bson.types.ObjectId
import org.mongodb.morphia.annotations.Id

import scala.beans.BeanProperty

/**
 * Created by pengyt on 2015/11/18.
 */
class RecommendCategory {

  @Id
  @BeanProperty
  var id: ObjectId = _

  @BeanProperty
  var categories: JList[String] = _
}
