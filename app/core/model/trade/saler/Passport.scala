package core.model.trade.saler

import javax.validation.constraints.NotNull

import core.model.geo.Country
import org.hibernate.validator.constraints.NotBlank

import scala.beans.BeanProperty

/**
 * 护照
 *
 * Created by zephyre on 10/21/15.
 */
class Passport extends IdProof {
  /**
   * 国籍
   */
  @NotNull
  @BeanProperty
  var nation: Country = null

  /**
   * 护照号码
   */
  @NotBlank
  @BeanProperty
  var number: String = null

}
