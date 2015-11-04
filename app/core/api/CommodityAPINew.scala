package core.api

import com.lvxingpai.model.marketplace.product.Commodity
import core.db.MorphiaFactory
import org.bson.types.ObjectId
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by pengyt on 2015/11/4.
 */
object CommodityAPINew {

  val ds = MorphiaFactory.datastore
  /**
   * 根据商品Id取得商品信息
   * @param cmyId
   * @return
   */
  def getCommodityById(cmyId: Long): Future[Commodity] = {
    val query = ds.createQuery(classOf[Commodity]).field("id").equal(cmyId)
    Future {
      query.get
    }
  }
}
