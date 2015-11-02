package com.lvxingpai.model.marketplace

import javax.validation.constraints.NotNull

import com.lvxingpai.model.geo.Country
import com.lvxingpai.model.misc.ImageItem
import org.hibernate.validator.constraints.NotBlank
import org.mongodb.morphia.annotations.Embedded
import java.util.{ List => JList }

/**
 *
 * Created by zephyre on 10/27/15.
 */
@Embedded
class IdProof {
  @NotBlank
  val proofType: String = null

  @NotBlank
  val surname: String = null

  @NotBlank
  val givenName: String = null

  @NotBlank
  val idNumber: String = null

  /**
   * 国籍
   */
  @NotNull
  val nationality: Country = null

  /**
   * 档案材料
   */
  val images: JList[ImageItem] = null
}

object IdProof {
  val PROOF_TYPE_CHINESE_ID = "cnid"
  val PROOF_TYPE_PASSPORT = "passport"
}
