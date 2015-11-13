package core.model.misc

import org.bson.types.ObjectId
import org.hibernate.validator.constraints.NotBlank
import org.mongodb.morphia.annotations.{ Id, Entity }

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
   * 推荐主题类型
   */
  @NotBlank
  @BeanProperty
  var recommendType: String = _

  /**
   * 话题类型
   */
  @NotBlank
  @BeanProperty
  var topicType: String = _

  /**
   * 话题标题
   */
  @NotBlank
  @BeanProperty
  var topicTitle: String = _

  /**
   * 商品id列表
   */
  @BeanProperty
  var commoditieIds: Seq[Long] = _
}
