package core.formatter.marketplace.order

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.marketplace.misc.{ BasicCoupon, Coupon }
import core.misc.Utils
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{ DateTime, DateTimeZone }

/**
 * Created by topy on 2016/2/19.
 */
class CouponSerializer extends JsonSerializer[Coupon] {

  override def serialize(coupon: Coupon, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("id", coupon.id.toString)
    gen.writeNumberField("userId", coupon.userId)
    gen.writeStringField("desc", coupon.desc)
    gen.writeNumberField("discount", Utils.getActualPrice(coupon.discount))
    gen.writeBooleanField("available", coupon.available)

    //    val fmt = new SimpleDateFormat("yyyy-MM-dd")
    //    gen.writeStringField("expire", if (coupon.expire != null) fmt.format(coupon.expire) else "")

    val expire = Option(coupon.expire) map (date => {
      val tzDate = new DateTime(date) toDateTime DateTimeZone.forID("Asia/Shanghai")
      val fmt = ISODateTimeFormat.date()
      tzDate.toString(fmt)
    }) getOrElse ""
    gen.writeStringField("expire", expire)

    coupon match {
      case s: BasicCoupon => gen.writeNumberField("threshold", s.threshold)
    }
    gen.writeEndObject()
  }
}