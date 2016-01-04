package core.api

import java.util
import java.util.Date

import com.lvxingpai.model.account.RealNameInfo
import com.lvxingpai.model.marketplace.order.{ OrderActivity, Order }
import com.lvxingpai.model.marketplace.product.{ CommoditySnapshot, Commodity }
import core.exception.ResourceNotFoundException
import org.bson.types.ObjectId
import org.joda.time.DateTime
import org.mongodb.morphia.Datastore

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by pengyt on 2015/11/4.
 */
object CommodityAPI {

  /**
   * 根据商品Id取得商品信息
   * @param cmyId
   * @return
   */
  def getCommodityById(cmyId: Long, version: Option[Long], fields: Seq[String] = Seq())(implicit ds: Datastore): Future[Option[Commodity]] = {
    val query = version match {
      case None => ds.createQuery(classOf[Commodity])
      case _ => ds.createQuery(classOf[CommoditySnapshot]).field("version").equal(version.get)
    }
    query.field("commodityId").equal(cmyId).field("status").equal("pub")
    if (fields.nonEmpty)
      query.retrievedFields(true, fields: _*)
    Future {
      Option(query.get)
    }
  }

  def createOrder(commodityId: Long, planId: String, rendezvous: Date, consumerId: Long,
    travellers: Seq[RealNameInfo], contact: RealNameInfo, quantity: Int, comment: String)(implicit ds: Datastore): Future[Option[Order]] = {
    val commoditySeq = Seq("commodityId", "title", "desc", "price", "plans", "seller", "category", "cover", "images", "version")
    for {
      commodityOpt <- CommodityAPI.getCommodityById(commodityId, version = None, commoditySeq)
    } yield {
      if (commodityOpt.isEmpty)
        throw ResourceNotFoundException("商品不存在或已下架")
      for {
        commodity <- commodityOpt
        plan <- commodity.plans.toSeq find (_.planId == planId) // 找到plan
        pricing <- plan.pricing.toSeq find (pricing => {
          // 找到价格
          val start = Option(pricing.timeRange.head) getOrElse new Date(0)
          val end = Option(pricing.timeRange.last) getOrElse new Date(2099, 1, 1)
          (rendezvous after start) && (rendezvous before end)
        })
      } yield {
        val order = new Order
        val now = DateTime.now().toDate
        order.id = new ObjectId
        order.orderId = now.getTime
        order.consumerId = consumerId
        order.commodity = commodity
        order.contact = contact
        order.planId = planId
        order.quantity = quantity
        // 设定订单价格
        order.totalPrice = quantity * pricing.price
        order.comment = comment
        order.rendezvousTime = rendezvous
        order.status = "pending"
        order.createTime = now
        order.updateTime = now
        // TODO 设置订单的失效时间为三天
        val expireDate = DateTime.now().plusDays(3)
        order.expireDate = expireDate.toDate
        order.travellers = travellers
        val act = new OrderActivity
        act.action = "create"
        act.timestamp = now
        act.prevStatus = ""
        act.data = Map[String, Any]("userId" -> consumerId)
        order.activities = util.Arrays.asList(act)
        ds.save[Order](order)
        order
      }
    }
  }

  /**
   * 根据商品Id取得商品信息
   * @param ids
   * @return
   */
  def getCommoditiesByIdList(ids: Seq[Long])(implicit ds: Datastore): Future[Seq[Commodity]] = {
    val query = ds.createQuery(classOf[Commodity]).field("commodityId").in(seqAsJavaList(ids)).field("status").equal("pub")
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
        .field("locality.id").equal(new ObjectId(localityId)).field("status").equal("pub")
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
      .retrievedFields(true, Seq("commodityId", "title", "marketPrice", "price", "rating", "salesVolume", "images", "cover", "locality"): _*)
    if (sellerId.nonEmpty)
      query.field("seller.sellerId").equal(sellerId.get)
    if (localityId.nonEmpty)
      query.field("locality.id").equal(new ObjectId(localityId.get))
    if (coType.nonEmpty && !coType.get.equals(""))
      query.field("category").hasThisOne(coType.get)
    val orderStr = if (sort.equals("asc")) sortBy else s"-$sortBy"
    query.field("status").equal("pub").order(orderStr).offset(start).limit(count)

    Future {
      query.asList()
    }
  }
}
