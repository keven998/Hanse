package core.model.misc

import java.util.{ List => JList }

import org.bson.types.ObjectId
import org.hibernate.validator.constraints.NotBlank
import org.mongodb.morphia.annotations.{ Entity, Id }

import scala.beans.BeanProperty

/**
 * Created by pengyt on 2015/11/13.
 */
@Entity
class TopicCommodity {

  /**
   * 专题商品列表
   */
  @Id
  var id: ObjectId = _

  /**
   * 话题类型
   */
  @NotBlank
  @BeanProperty
  var topicType: String = _

  /**
   * 商品id列表
   */
  @BeanProperty
  var commodities: JList[Long] = _
}
