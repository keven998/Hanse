package core.formatter.user

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.model.account.UserInfo
import com.lvxingpai.model.misc.ImageItem

/**
 * Created by pengyt on 2015/11/17.
 */
class UserSerializer extends JsonSerializer[UserInfo] {

  override def serialize(user: UserInfo, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeNumberField("userId", Option(user.userId) getOrElse 0L)
    gen.writeStringField("nickname", Option(user.nickname) getOrElse "")

    gen.writeFieldName("avatar")
    val avatar = user.avatar
    val retAvatar = if (avatar != null) serializers.findValueSerializer(classOf[ImageItem], null)
    else serializers.findNullValueSerializer(null)
    retAvatar.serialize(avatar, gen, serializers)

    gen.writeEndObject()
  }
}
