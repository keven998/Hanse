package core.model.misc

import org.mongodb.morphia.annotations.Embedded

/**
 * Created by pengyt on 2015/11/13.
 */
@Embedded
class ColumnGroup {

  /**
   * 专题类型
   */
  var columnType: String = _

  /**
   * 运营位列表
   */
  var columns: Seq[Column] = _
}
