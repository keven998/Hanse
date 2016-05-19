package core.model.misc

import java.util.{ List => JList }

import org.bson.types.ObjectId
import org.mongodb.morphia.annotations.{ Entity, Id }

/**
 *
 * Created by topy on 2016/5/12.
 */
@Entity
class MiscInfo {
  @Id
  var id: ObjectId = _

  var key: String = _

  var value: String = _

}

object MiscInfo {

  val KEY_BOUNTY_IMG = "KEY_BOUNTY_IMG"

}

