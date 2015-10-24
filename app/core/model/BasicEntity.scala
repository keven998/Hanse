package core.model

import org.bson.types.ObjectId
import org.hibernate.validator.constraints.NotBlank
import org.mongodb.morphia.annotations.Id

import scala.beans.BeanProperty

/**
 * Created by zephyre on 10/19/15.
 */
class BasicEntity {
  /**
   * 主键
   */
  @NotBlank
  @BeanProperty
  @Id
  var id: ObjectId = null
}
