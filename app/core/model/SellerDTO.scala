package core.model

import com.lvxingpai.model.marketplace.seller.Seller
import com.lvxingpai.yunkai.UserInfo

/**
 *
 * Created by topy on 2015/11/5.
 */
class SellerDTO extends Seller {

  var avatar: String = _

  var nickName: String = _

  var signature: String = _

  var userPhone: String = _

}

object SellerDTO {

  def apply(s: Seller, u: UserInfo) = {
    val sDTO = new SellerDTO
    sDTO.avatar = u.avatar.getOrElse("")
    sDTO.nickName = u.nickName
    sDTO.signature = u.signature.getOrElse("")
    sDTO.userPhone = u.tel.getOrElse("")
    sDTO.id = s.id
    sDTO.shopTitle = s.shopTitle
    sDTO.lang = s.lang
    sDTO.serviceZones = s.serviceZones
    sDTO.phone = s.phone
    sDTO.address = s.address
    sDTO
  }

  def apply(s: Seller) = {
    val sDTO = new SellerDTO
    sDTO.avatar = ""
    sDTO.nickName = ""
    sDTO.signature = ""
    sDTO.id = s.id
    sDTO.shopTitle = s.shopTitle
    sDTO.lang = s.lang
    sDTO.serviceZones = s.serviceZones
    sDTO.phone = s.phone
    sDTO.address = s.address
    sDTO
  }
}
