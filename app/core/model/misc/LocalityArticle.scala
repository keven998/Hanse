package core.model.misc

import com.lvxingpai.model.geo.{ Country, Locality }
import com.lvxingpai.model.mixin.TimestampEnabled
import org.mongodb.morphia.annotations.Embedded

/**
 *
 * Created by topy on 2016/5/19.
 */
@Embedded
class LocalityArticle extends TimestampEnabled {
  /**
   * 标题
   */
  var title: String = _

  /**
   * 描述
   */
  var desc: String = _
  /**
   * 内容
   */
  var contents: String = _

  /**
   * 文章ID
   */
  var articleId: Long = _

  /**
   * 商品所属目的地
   */
  var locality: Locality = _

  /**
   * 商品所属国家
   */
  var country: Country = _

  /**
   * 状态
   */
  var status: String = _

}
