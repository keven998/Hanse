package core.model

import org.bson.types.ObjectId
import org.hibernate.validator.constraints.NotBlank

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
  var id: ObjectId = null
}
