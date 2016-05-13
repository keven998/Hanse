package core.model.misc


import java.util.{List => JList}

import com.lvxingpai.model.misc.ImageItem
import org.bson.types.ObjectId
import org.mongodb.morphia.annotations.{Entity, Id}

/**
 * Created by topy on 2016/5/12.
 */
@Entity
class BountyStats {
  @Id
  var id: ObjectId = _

  /**
   * 商家列表
   *
   */
  var images: JList[ImageItem] = _

}
