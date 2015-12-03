package core.api

import com.lvxingpai.model.marketplace.product.Commodity
import org.bson.types.ObjectId
import org.mongodb.morphia.Datastore

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by pengyt on 2015/11/4.
 */
object CommodityAPI {

  val COMMODITY_CATEGORY_ALL = "全部"

  /**
   * 根据商品Id取得商品信息
   * @param cmyId
   * @return
   */
  def getCommodityById(cmyId: Long, fields: Seq[String] = Seq())(implicit ds: Datastore): Future[Commodity] = {
    val query = ds.createQuery(classOf[Commodity]).field("commodityId").equal(cmyId)
    if (fields.nonEmpty)
      query.retrievedFields(true, fields: _*)
    Future {
      query.get
    }
  }

  def getCommoditySnapsById(cmyId: Long, planId: String)(implicit ds: Datastore): Future[Commodity] = {
    val query = ds.createQuery(classOf[Commodity]).field("commodityId").equal(cmyId)
      .retrievedFields(true, Seq("commodityId", "title", "desc", "price", "plans", "seller", "category"): _*)
    Future {
      val ret = query.get
      val plan = ret.plans.filter(_.planId.equals(planId)).toList
      ret.plans = plan
      ret
    }
  }

  /**
   * 根据商品Id取得商品信息
   * @param ids
   * @return
   */
  def getCommoditiesByIdList(ids: Seq[Long])(implicit ds: Datastore): Future[Seq[Commodity]] = {
    val query = ds.createQuery(classOf[Commodity]).field("commodityId").in(seqAsJavaList(ids))
    Future {
      query.asList()
    }
  }

  /**
   * 根据目的地查找商品分类
   * @param localityId 目的地id
   * @return 商品分类
   */
  def getCommodityCategories(localityId: String)(implicit ds: Datastore): Future[Seq[Commodity]] = {
    Future {
      val query = ds.createQuery(classOf[Commodity]).retrievedFields(true, Seq("commodityId", "category"): _*)
        .field("locality.id").equal(new ObjectId(localityId))
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
  def getCommodities(sellerId: Option[Long], localityId: Option[String], coType: Option[String], sortBy: String, sort: String, start: Int, count: Int)(implicit ds: Datastore): Future[Seq[Commodity]] = {
    val query = ds.createQuery(classOf[Commodity])
      .retrievedFields(true, Seq("commodityId", "title", "marketPrice", "price", "rating", "salesVolume", "images", "cover", "seller", "locality"): _*)
    if (sellerId.nonEmpty)
      query.field("seller.sellerId").equal(sellerId.get)
    if (localityId.nonEmpty)
      query.field("locality.id").equal(new ObjectId(localityId.get))
    if (coType.nonEmpty && !coType.get.equals("") && !coType.get.equals(COMMODITY_CATEGORY_ALL))
      query.field("category").hasThisOne(coType.get)
    val orderStr = if (sort.equals("asc")) sortBy else s"-$sortBy"
    query.order(orderStr).offset(start).limit(count)

    Future {
      query.asList()
    }
  }
}
