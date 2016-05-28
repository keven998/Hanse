package core.model.misc

import com.lvxingpai.model.mixin.{ ImagesEnabled, RankEnabled }
import org.bson.types.ObjectId
import org.mongodb.morphia.annotations.{ Entity, Id }

import scala.beans.BeanProperty

/**
 * 悬赏的封面图
 *
 * Created by pengyt on 2015/11/13.
 */
@Entity
class ColumnBounty extends ImagesEnabled with RankEnabled {

  @Id
  @BeanProperty
  var id: ObjectId = _

  /**
   * 标题
   */
  @BeanProperty
  var title: String = _

  /**
   * 链接
   */
  @BeanProperty
  var link: String = _

  /**
   * 是否上线 pub-上线
   */
  @BeanProperty
  var status: String = _
}
