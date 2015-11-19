package core.api

import com.lvxingpai.model.marketplace.product.Commodity
import com.mongodb.BasicDBObjectBuilder
import core.db.MorphiaFactory
import org.bson.types.ObjectId

import scala.collection.JavaConversions._
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

  /**
   * 根据商品Id取得商品信息
   * @param ids
   * @return
   */
  def getCommoditiesByIdList(ids: Seq[Long]): Future[Seq[Commodity]] = {
    val query = ds.createQuery(classOf[Commodity]).field("id").in(ids)
    Future {
      query.asList()
    }
  }

  /**
   * 根据目的地查找商品分类
   * @param localityId 目的地id
   * @return 商品分类
   */
  def getCommodityCategoryList(localityId: String): Future[Seq[String]] = {

    Future {
      val col = MorphiaFactory.getCollection(classOf[Commodity])
      val fieldName = "category"

      val query = BasicDBObjectBuilder.start().add("locality.id", new ObjectId(localityId)).get()
      val fields = BasicDBObjectBuilder.start(Map("category" -> 1)).get()

      //      val doc = col.aggregate()

      null
    }
  }

  /**
   * 根据店铺id查找商品列表
   * @param sellerId 店铺id
   * @param sortBy 比如：按照销量排序
   * @param sort 正序或者逆序
   * @param start 返回商品列表的起始位置
   * @param count 返回商品的个数
   * @return 返回商品列表
   */
  def getCommoditiesBySellerId(sellerId: Long, sortBy: String, sort: String, start: Int, count: Int): Future[Seq[Commodity]] = {
    val query = ds.createQuery(classOf[Commodity]).field("seller.sellerId").equal(sellerId).offset(start).limit(count)
    Future {
      query.asList()
    }
  }

  /**
   * 根据店铺id查找商品列表
   * @param localityId 店铺id
   * @param sortBy 比如：按照销量排序
   * @param sort 正序或者逆序
   * @param start 返回商品列表的起始位置
   * @param count 返回商品的个数
   * @return 返回商品列表
   */
  def getCommoditiesByLocalityId(localityId: String, sortBy: String, sort: String, start: Int, count: Int): Future[Seq[Commodity]] = {
    val query = ds.createQuery(classOf[Commodity]).field("locality.id").equal(new ObjectId(localityId)).offset(start).limit(count)
    Future {
      query.asList()
    }
  }
}
