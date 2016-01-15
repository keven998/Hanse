package core.api

import java.util
import java.util.Date

import com.lvxingpai.model.account.RealNameInfo
import com.lvxingpai.model.marketplace.order.{ Order, OrderActivity }
import com.lvxingpai.model.marketplace.product.{ Commodity, CommoditySnapshot }
import core.exception.ResourceNotFoundException
import core.formatter.marketplace.order.OrderFormatter
import core.service.ViaeGateway
import org.apache.commons.lang.StringUtils
import org.bson.types.ObjectId
import org.joda.time.{ DateTime, DateTimeZone, LocalDate }
import org.mongodb.morphia.Datastore
import play.api.Play
import play.api.Play.current

import scala.collection.JavaConversions._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.language.postfixOps

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
      case None => ds.createQuery(classOf[Commodity]).field("status").equal("pub")
      case _ => ds.createQuery(classOf[CommoditySnapshot]).field("version").equal(version.get)
    }
    query.field("commodityId").equal(cmyId)
    if (fields.nonEmpty)
      query.retrievedFields(true, fields: _*)
    Future {
      Option(query.get)
    }
  }

  private def ts2LocalDate(range: Seq[Date], zone: DateTimeZone): (LocalDate, LocalDate) = {
    // 将时间戳类型的数据, 转换成LocalDate类型的Tuple2
    val s = range.head.getTime
    val e = range.last.getTime
    (Seq(0, 1) -> Seq(s, e)).zipped map {
      case (index, ts) => Option(ts) map (new DateTime(_, zone).toLocalDate) getOrElse (index match {
        case 0 => new LocalDate(1970, 1, 1)
        case _ => new LocalDate(2099, 12, 31)
      })
    } match {
      case Seq(start, end) => start -> end
    }
  }

  private def isWithinRange(date: LocalDate, range: Seq[Date],
    zone: DateTimeZone = DateTimeZone.forID("Asia/Shanghai")): Boolean = {
    // 转换range
    if (range.length != 2)
      false
    else {
      val (start, end) = ts2LocalDate(range, zone)
      (date.isAfter(start) || date.isEqual(start)) && (date.isBefore(end) || date.isEqual(end))
    }
  }

  def createOrder(commodityId: Long, planId: String, rendezvous: LocalDate, consumerId: Long,
    travellers: Seq[RealNameInfo], contact: RealNameInfo, quantity: Int, comment: String)(implicit ds: Datastore): Future[Option[Order]] = {
    val commoditySeq = Seq("_id", "commodityId", "title", "desc", "price", "plans", "seller", "category", "cover", "images", "version")
    val future = for {
      commodityOpt <- CommodityAPI.getCommodityById(commodityId, version = None, commoditySeq)
    } yield {
      if (commodityOpt.isEmpty)
        throw ResourceNotFoundException("商品不存在或已下架")

      for {
        commodity <- commodityOpt
        plan <- commodity.plans.toSeq find (_.planId == planId) // 找到plan
        pricing <- { // 查找符合条件的日期区间
          val zone = DateTimeZone.forID("Asia/Shanghai")

          Option(plan.pricing) flatMap (value => {
            value find (pricing => {
              Option(pricing.timeRange) map (_.toSeq) exists (isWithinRange(rendezvous, _, zone))
            })
          })
        }
      } yield {
        // 设置选定的价格
        plan.pricing = Seq(pricing)
        // 设置选定的套餐
        commodity.plans = Seq(plan)
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
        order.rendezvousTime = rendezvous.toDate
        order.status = "pending"
        order.createTime = now
        order.updateTime = now
        // TODO 设置订单的失效时间为三天
        val expireDate = DateTime.now().plusDays(3)
        order.expireDate = expireDate.toDate
        order.travellers = travellers
        val act = new OrderActivity
        act.action = "create"
        act.prevStatus = StringUtils.EMPTY
        act.timestamp = now
        act.data = Map[String, Any]("userId" -> consumerId)
        order.activities = util.Arrays.asList(act)
        ds.save[Order](order)

        val orderNode = OrderFormatter.instance.formatJsonNode(order)

        val viae = Play.application.injector instanceOf classOf[ViaeGateway]
        viae.sendTask("viae.event.marketplace.onCreateOrder", kwargs = Some(Map("order" -> orderNode)))

        order
      }
    }

    future
  }

  def getCommoditiesByObjectIdList(ids: Seq[ObjectId], fields: Seq[String])(implicit ds: Datastore): Future[Option[Seq[Commodity]]] = {
    Future {
      if (ids.isEmpty)
        None
      else {
        Option(ds.createQuery(classOf[Commodity]).field("id").in(seqAsJavaList(ids)).retrievedFields(true, fields: _*).asList())
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
      .retrievedFields(true, Seq("_id", "commodityId", "title", "marketPrice", "price", "rating", "salesVolume", "images", "cover", "locality", "seller"): _*)
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
