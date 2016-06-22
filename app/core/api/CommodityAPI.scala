package core.api

import java.util
import java.util.Date

import com.lvxingpai.model.account.{ RealNameInfo, UserInfo }
import com.lvxingpai.model.marketplace.misc.Coupon
import com.lvxingpai.model.marketplace.order.{ Order, OrderActivity }
import com.lvxingpai.model.marketplace.product.{ EmbeddedCommodity, Commodity, CommodityComment, CommoditySnapshot }
import com.lvxingpai.model.misc.ImageItem
import com.lvxingpai.yunkai.{ UserInfo => YunkaiUser }
import core.exception.{ CommodityStatusException, ResourceNotFoundException }
import core.formatter.marketplace.order.OrderFormatter
import core.model.misc.GeoCommodity
import core.search._
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

  def getAllCommodities()(implicit ds: Datastore): Future[Seq[Commodity]] = {
    val query = ds.createQuery(classOf[Commodity]).field("commodityType").equal("original").field("status").equal("pub")
      .retrievedFields(true, Seq("_id", "status", "commodityId", "seller", "country", "locality"): _*)
    Future {
      query.asList()
    }
  }

  def saveCountrySellers(all: Seq[Commodity])(implicit ds: Datastore): Future[Unit] = {
    val countryMap = all.groupBy(_.country)
    val cCountry = countryMap.filter(_._1 != null).map(x => {
      val geoCommodity = new GeoCommodity
      geoCommodity.geoId = x._1.id
      geoCommodity.sellers = x._2.map(_.seller)
      geoCommodity
    }) map (c => {
      c.sellers = c.sellers.groupBy(_.sellerId).map(_._2.head).toList
      c
    })
    Future {
      ds.save[GeoCommodity](cCountry.toList)
    }
  }

  def saveLocalitySellers(all: Seq[Commodity])(implicit ds: Datastore): Future[Unit] = {
    val countryMap = all.groupBy(_.locality)
    val cCountry = countryMap.filter(_._1 != null).map(x => {
      val geoCommodity = new GeoCommodity
      geoCommodity.geoId = x._1.id
      geoCommodity.sellers = x._2.map(_.seller)
      geoCommodity
    }) map (c => {
      c.sellers = c.sellers.groupBy(_.sellerId).map(_._2.head).toList
      c
    })
    Future {
      ds.save[GeoCommodity](cCountry.toList)
    }
  }

  def getGeoSeller(id: String)(implicit ds: Datastore): Future[GeoCommodity] = {
    val query = ds.createQuery(classOf[GeoCommodity]).field("geoId").equal(new ObjectId(id))
    Future {
      query.get
    }
  }

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

  def modCommodity(cmyId: Long, userId: Long, status: Option[String])(implicit ds: Datastore): Future[Int] = {
    val cmyQuery = ds.createQuery(classOf[Commodity]) field "commodityId" equal cmyId field "seller.sellerId" equal userId
    val ups = ds.createUpdateOperations(classOf[Commodity])
    Future {
      val ret = status match {
        case Some("pub") | Some("disabled") =>
          ups.set("status", status.get)
          ds.update(cmyQuery, ups)
        case _ => throw CommodityStatusException("非法的商品状态")
      }
      ret.getUpdatedCount
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
    travellers: Seq[RealNameInfo], contact: RealNameInfo, quantity: Int, comment: String,
    coupons: Seq[ObjectId])(implicit ds: Datastore): Future[Option[Order]] = {
    val commoditySeq = Seq("_id", "commodityId", "title", "price", "plans", "seller", "category", "cover", "images", "version")
    val future = for {
      commodityOpt <- CommodityAPI.getCommodityById(commodityId, version = None, commoditySeq)
      couponOpt <- coupons.headOption map OrderAPI.getCoupon getOrElse Future.successful(None) // 优惠券
    } yield {
      if (commodityOpt.isEmpty)
        throw ResourceNotFoundException("商品不存在或已下架")

      for {
        commodity <- commodityOpt
        plan <- commodity.plans.toSeq find (_.planId == planId) // 找到plan
        pricing <- {
          // 查找符合条件的日期区间
          val zone = DateTimeZone.forID("Asia/Shanghai")

          Option(plan.pricing) flatMap (value => {
            value find (pricing => {
              Option(pricing.timeRange) map (_.toSeq) exists (isWithinRange(rendezvous, _, zone))
            })
          })
        }
      } yield {
        import com.lvxingpai.model.marketplace.product.CommodityConversion._

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
        order.discount = couponOpt map (_.discount) getOrElse 0
        order.comment = comment
        order.rendezvousTime = rendezvous.toDate
        order.status = "pending"
        order.createTime = now
        order.updateTime = now
        val expireDate = DateTime.now().plusHours(2)
        order.expireDate = expireDate.toDate
        order.travellers = travellers
        val act = new OrderActivity
        act.action = "create"
        act.prevStatus = StringUtils.EMPTY
        act.timestamp = now
        act.data = Map[String, Any]("userId" -> consumerId)
        order.activities = util.Arrays.asList(act)
        ds.save[Order](order)

        // 删除使用过后的优惠券
        couponOpt foreach (coupon => {
          coupon.available = false
          ds.save[Coupon](coupon)
        })

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
  def getCommoditiesByIdList(ids: Seq[Long], isSeller: Boolean = false)(implicit ds: Datastore): Future[Seq[Commodity]] = {
    val query = ds.createQuery(classOf[Commodity]).field("commodityId").in(seqAsJavaList(ids))
    if (!isSeller)
      query.field("status").equal("pub")
    Future {
      query.asList().sortBy(c => {
        // 按照出现在ids中的位置进行排序
        val index = ids.indexOf(c.commodityId)
        if (index != -1)
          index
        else
          Int.MaxValue
      })
    }
  }

  /**
   * 根据目的地查找商品分类
   * @param localityId 目的地id
   * @return 商品分类
   */
  def getCommodityCategories(localityId: ObjectId)(implicit ds: Datastore): Future[Seq[Commodity]] = {
    Future {
      val query = ds.createQuery(classOf[Commodity]).retrievedFields(true, Seq("commodityId", "category"): _*)
        .field("locality.id").equal(localityId).field("status").equal("pub")
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
  def getCommodities(sellerId: Option[Long], localityId: Option[String], category: Option[String], cType: Option[String], sortBy: String, sort: String, start: Int, count: Int)(implicit ds: Datastore): Future[Seq[Commodity]] = {
    val query = ds.createQuery(classOf[Commodity])
      .retrievedFields(true, Seq("_id", "status", "commodityId", "title", "marketPrice", "price", "rating", "salesVolume", "images", "cover", "locality", "seller"): _*)
    if (sellerId.nonEmpty)
      query.field("seller.sellerId").equal(sellerId.get)
    if (localityId.nonEmpty)
      //query.field("locality.id").equal(new ObjectId(localityId.get))
      query.or(
        query.criteria("country.id").equal(new ObjectId(localityId.get)),
        query.criteria("locality.id").equal(new ObjectId(localityId.get))
      )
    if (category.nonEmpty && !category.get.equals(""))
      query.field("category").hasThisOne(category.get)
    if (cType.nonEmpty && !cType.get.equals(""))
      query.field("commodityType").equal(cType.get)
    val orderStr = if (sort.equals("asc")) sortBy else s"-$sortBy"
    query.field("status").equal("pub").order(orderStr).offset(start).limit(count)
    Future {
      query.asList()
    }
  }

  /**
   * 使用搜索引擎, 搜索商品
   * @return
   */
  def searchCommodities(q: Option[String], sellerId: Option[Long], localityId: Option[String],
    category: Option[String], status: Option[String], cType: Option[String], sortBy: String, sort: String, start: Int, count: Int, isSeller: Boolean)(implicit ds: Datastore): Future[Seq[Commodity]] = {
    val es = Play.application.injector instanceOf classOf[SearchEngine]

    // 根据商品状态筛选
    val statusFilter = Some(CommodityStatusFilter())
    // 按照商家筛选
    val sellerFilter = sellerId map SellerFilter.apply
    // 按照城市筛选
    val localityFilter = localityId map LocalityFilter.apply
    // 按照类别筛选
    val categoryFilter = category map CategoryFilter.apply
    // 根据商品的类别选-1.original:原始类型的商品 2.scheme:方案商品
    val cTypeFilter = cType map CommodityTypeFilter.apply
    // 商家根据不同的商品状态筛选
    val statusOptFilter = status map CommodityStatusOptFilter.apply

    val filters = (Seq(sellerFilter, localityFilter, categoryFilter, cTypeFilter) ++ (if (isSeller) Seq(statusOptFilter) else Seq(statusFilter))) filter (_.nonEmpty) map (_.get)

    es.overallCommodities(q, filters, sortBy, sort, start, count) flatMap (clist => {
      val idList = clist map (_.commodityId)
      if (idList.nonEmpty)
        getCommoditiesByIdList(idList, isSeller = true)
      else
        Future.successful(Seq())
    })
  }

  /**
   * 针对某件商品发表评论
   *
   * @param commodityId 商品ID
   * @param user 用户信息
   * @param contents 评论内容
   * @param rating 对商品的评分
   * @param img 评论商品的时候, 可以上传一些照片
   * @return
   */
  def postComment(commodityId: Long, user: YunkaiUser, contents: String, rating: Option[Float],
    img: Option[Seq[ImageItem]], orderId: Option[Long], anonymous: Boolean)(implicit ds: Datastore): Future[Unit] = {
    // 必须购买才能评论
    (for {
      order <- getBoughtOrder(orderId, user.userId)
    } yield {
      // TODO 临时修改为未购买也可评价
      // if (order.nonEmpty) {
      val comment = new CommodityComment()
      comment.id = new ObjectId()
      if (order.nonEmpty)
        comment.order = order.get
      else {
        val commodity = new EmbeddedCommodity
        commodity.commodityId = commodityId
        val order = new Order
        order.commodity = commodity
        comment.order = order
      }
      comment.contents = contents

      val userInfo = new UserInfo
      userInfo.nickname = user.nickName
      user.avatar foreach (avatar => {
        if (avatar.nonEmpty && (avatar startsWith "http")) {
          val item = new ImageItem
          item.url = avatar
          userInfo.avatar = item
        }
      })
      userInfo.userId = user.userId
      comment.user = userInfo

      rating foreach (comment.rating = _)
      img foreach (comment.images = _)
      val now = DateTime.now().toDate
      comment.createTime = now
      comment.updateTime = now
      comment.anonymous = anonymous
      ds.save[CommodityComment](comment)
      comment
      //      } else {
      //        // 如果没有购买商品, 则没有资格评论
      //        throw OrderStatusException(s"User ${user.userId} have not bought commodity $commodityId yet, " +
      //          s"cannot post comments.")
      //      }
    })
    //    map (_ => {
    //
    //      // 刷新订单状态为已评价
    //      val statusQuery = ds.createQuery(classOf[Order]) field "orderId" equal orderId.get
    //      val statusOps = ds.createUpdateOperations(classOf[Order]).set("status", Order.Status.Reviewed.toString)
    //      ds.update(statusQuery, statusOps)
    //    })
  }

  /**
   * 判断一个用户是否购买了某件商品
   *
   * @param orderId 订单ID
   * @param userId 用户ID
   * @return
   */
  def getBoughtOrder(orderId: Option[Long], userId: Long)(implicit ds: Datastore): Future[Option[Order]] = {
    Future {
      if (orderId.nonEmpty)
        Option(ds.createQuery(classOf[Order])
          .field("orderId").equal(orderId.get)
          .field("consumerId").equal(userId)
          // TODO 临时开放评论接口,任何人都可以评论
          //.field("status").equal(Order.Status.ToReview.toString)
          .retrievedFields(true, Seq("orderId", "commodity", "status"): _*).get)
      else None
    }
  }

  /**
   * 获得某个商品的评论列表
   * @param commodityId 商品ID
   * @param start 分页
   * @param count 分页
   * @return
   */
  def getComments(commodityId: Long, start: Int, count: Int)(implicit ds: Datastore): Future[Seq[CommodityComment]] = {
    Future {
      val query = ds.createQuery(classOf[CommodityComment]).field("order.commodity.commodityId").equal(commodityId)
        .offset(start).limit(count).order("-createTime")
      query.asList()
    }
  }

  def getCommentsCnt(commodityId: Long)(implicit ds: Datastore): Future[Long] = {
    Future {
      ds.createQuery(classOf[CommodityComment]).field("order.commodity.commodityId").equal(commodityId).countAll()
    }
  }
}
