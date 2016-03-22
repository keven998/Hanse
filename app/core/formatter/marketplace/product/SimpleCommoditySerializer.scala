package core.formatter.marketplace.product

import java.util.Date

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.geo.Locality
import com.lvxingpai.model.marketplace.product.Commodity
import com.lvxingpai.model.marketplace.seller.Seller
import com.lvxingpai.model.misc.ImageItem
import core.misc.Utils
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{ DateTime, DateTimeZone, LocalDate }

import scala.collection.JavaConversions._

/**
 * Created by topy on 2015/11/13.
 */
class SimpleCommoditySerializer extends JsonSerializer[Commodity] {

  override def serialize(commodity: Commodity, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()
    gen.writeStringField("id", commodity.id.toString)
    gen.writeNumberField("commodityId", commodity.commodityId)

    gen.writeStringField("title", Option(commodity.title) getOrElse "")
    gen.writeNumberField("marketPrice", Utils.getActualPrice(commodity.marketPrice))
    gen.writeNumberField("price", Utils.getActualPrice(commodity.price))
    gen.writeNumberField("salesVolume", commodity.salesVolume)
    gen.writeNumberField("rating", commodity.rating)

    gen.writeFieldName("seller")
    val userInfo = commodity.seller
    val retUserInfo = if (userInfo != null) serializers.findValueSerializer(classOf[Seller], null)
    else serializers.findNullValueSerializer(null)
    retUserInfo.serialize(userInfo, gen, serializers)

    gen.writeFieldName("locality")
    val loc = commodity.locality
    val retLoc = if (loc != null) serializers.findValueSerializer(classOf[Locality], null)
    else serializers.findNullValueSerializer(null)
    retLoc.serialize(loc, gen, serializers)

    //    gen.writeFieldName("images")
    //    gen.writeStartArray()
    //    val images = commodity.images
    //    if (images.nonEmpty) {
    //      val ret = serializers.findValueSerializer(classOf[ImageItem], null)
    //      for (image <- images)
    //        ret.serialize(image, gen, serializers)
    //    }
    //    gen.writeEndArray()

    gen.writeFieldName("cover")
    val cover = commodity.cover
    val retCover = if (cover != null) serializers.findValueSerializer(classOf[ImageItem], null)
    else serializers.findNullValueSerializer(null)
    retCover.serialize(cover, gen, serializers)

    // 判断商品的可用时间是否已经过期
    val zone = DateTimeZone.forID("Asia/Shanghai")
    val pricingSeq = Option(commodity.plans) map (_.toSeq) getOrElse Seq() flatMap (_.pricing)
    val now = Seq(DateTime.now().getYear, DateTime.now().getMonthOfYear, DateTime.now().getDayOfMonth) mkString "-"
    //    预约时间假定为当前时间
    val rendezvous = ISODateTimeFormat.date parseLocalDate now
    val ret = Option(pricingSeq) flatMap (value => {
      value find (pricing => {
        Option(pricing.timeRange) map (_.toSeq) exists (isWithinRange(rendezvous, _, zone))
      })
    })
    gen.writeBooleanField("expire", ret.isEmpty)

    gen.writeStringField("status", Option(commodity.status) getOrElse "")
    gen.writeNumberField("createTime", if (commodity.createTime != null) commodity.createTime.getTime else 0)
    gen.writeNumberField("updateTime", if (commodity.updateTime != null) commodity.updateTime.getTime else 0)

    gen.writeEndObject()
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
}
