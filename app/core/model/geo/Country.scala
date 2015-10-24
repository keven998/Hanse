package core.model.geo

import javax.validation.constraints.{ Min, NotNull, Pattern }

import core.model.mixin.{ RankEnabled, ImagesEnabled }
import org.hibernate.validator.constraints.NotBlank
import java.util.{ List => JList }
import scala.beans.BeanProperty

/**
 * 国家
 * Created by pengyt on 2015/10/19.
 */
class Country extends GeoEntity with ImagesEnabled with RankEnabled {

  /**
   * ISO 3166-2标准的国家代码
   */
  @NotBlank
  @Pattern(regexp = "[A-Z]{2}")
  @BeanProperty
  var code: String = null

  /**
   * ISO 3166-3标准的国家代码
   */
  @NotBlank
  @Pattern(regexp = "[A-Z]{3}")
  @BeanProperty
  var code3: String = null

  /**
   * 所属大洲
   */
  @NotNull
  @BeanProperty
  var continent: Continent = null

  /**
   * 中文名称
   */
  @NotBlank
  @BeanProperty
  var zhName: String = null

  /**
   * 英文名称
   */
  @NotBlank
  @BeanProperty
  var enName: String = null

  /**
   * 描述
   */
  @BeanProperty
  var desc: String = null

  /**
   * 别名
   */
  @BeanProperty
  var alias: JList[String] = null

  /**
   * 电话的国家代码
   */
  @Min(value = 1)
  @BeanProperty
  var dialCode: Int = 0

  /**
   * 语言
   */
  @BeanProperty
  var lang: JList[String] = null
}
