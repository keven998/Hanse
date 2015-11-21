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

    gen.writeBooleanField("domestic", Option(bankAccount.domestic) getOrElse false)
    gen.writeStringField("swift", Option(bankAccount.swift) getOrElse "")
    gen.writeStringField("accountNumber", Option(bankAccount.accountNumber) getOrElse "")
    gen.writeStringField("bankName", Option(bankAccount.bankName) getOrElse "")
    gen.writeStringField("branchName", Option(bankAccount.branchName) getOrElse "")
    gen.writeStringField("cardHolder", Option(bankAccount.cardHolder) getOrElse "")

    gen.writeStringField("billingAddress", Option(bankAccount.billingAddress) getOrElse "")

    gen.writeEndObject()
  }
}
