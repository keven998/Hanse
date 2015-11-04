package core.formatter.marketplace.seller

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import com.lvxingpai.model.marketplace.seller.BankAccount

/**
 * Created by pengyt on 2015/11/4.
 */
class BankAccountSerializer extends JsonSerializer[BankAccount] {

  override def serialize(bankAccount: BankAccount, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeBooleanField("domestic", bankAccount.domestic)
    if (bankAccount.swift != null)
      gen.writeStringField("swift", bankAccount.swift)
    gen.writeStringField("accountNumber", bankAccount.accountNumber)
    if (bankAccount.bankName != null)
      gen.writeStringField("bankName", bankAccount.bankName)
    if (bankAccount.branchName != null)
      gen.writeStringField("branchName", bankAccount.branchName)

    gen.writeStringField("cardHolder", bankAccount.cardHolder)

    if (bankAccount.billingAddress != null)
      gen.writeStringField("billingAddress", bankAccount.billingAddress)

    gen.writeEndObject()
  }
}
