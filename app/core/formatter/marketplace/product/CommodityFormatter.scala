package core.formatter.marketplace.product

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.lvxingpai.model.marketplace.product.Commodity
import com.lvxingpai.model.marketplace.seller.{BankAccount, Seller}
import com.lvxingpai.model.misc.{PhoneNumber, RichText}
import core.formatter.BaseFormatter
import core.formatter.marketplace.seller.{BankAccountSerializer, SellerSerializer}
import core.formatter.misc.{PhoneNumberSerializer, RichTextSerializer}

/**
 * Created by pengyt on 2015/11/3.
 */
class CommodityFormatter extends BaseFormatter {

  override val objectMapper = {
    val mapper = new ObjectMapper()
    val module = new SimpleModule()
    module.addSerializer(classOf[RichText], new RichTextSerializer)
    module.addSerializer(classOf[Commodity], new CommoditySerializer)
    module.addSerializer(classOf[BankAccount], new BankAccountSerializer)
    module.addSerializer(classOf[Seller], new SellerSerializer)
    module.addSerializer(classOf[PhoneNumber], new PhoneNumberSerializer)
    mapper.registerModule(module)
    mapper
  }
}
