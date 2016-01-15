package core.formatter.marketplace.product

import java.util.Date

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.marketplace.product.Pricing
import core.misc.Utils
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{ DateTime, DateTimeZone }

import scala.collection.JavaConversions._
import scala.language.postfixOps

/**
 * Created by pengyt on 2015/11/4.
 */
class PricingSerializer(zone: DateTimeZone = DateTimeZone.forID("Asia/Shanghai")) extends JsonSerializer[Pricing] {

  override def serialize(pricing: Pricing, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeNumberField("price", Utils.getActualPrice(pricing.price))

    gen.writeFieldName("timeRange")
    gen.writeStartArray()

    /**
     * 将一个date转换成字符串. 如果date为None, 则: 如果index为0, 表示1970年, 如果index为1, 表示2099年
     * @param dt
     * @param index
     * @return
     */
    def transformDate(dt: Option[Date], index: Int): String = {
      // 转换为joda
      val jodaDate: DateTime = dt map (new DateTime(_)) getOrElse (index match {
        case 0 => new DateTime(0)
        case _ => new DateTime(2099, 12, 31, 23, 59, 59, DateTimeZone.forID("UTC"))
      }) toDateTime zone

      val fmt = ISODateTimeFormat.dateTimeNoMillis()
      jodaDate.toString(fmt)
    }

    // 将pricing.timeRange写入
    Option(pricing.timeRange) flatMap (v => {
      val range = v.toSeq
      if (range.length != 2) {
        None
      } else {
        val strings = (Seq(0, 1) -> range).zipped map {
          case (index, dt) => transformDate(Option(dt), index)
        }
        Some(strings)
      }
    }) foreach (v => v foreach (gen writeString _))

    gen.writeEndArray()

    gen.writeEndObject()
  }
}
