package core.formatter.user

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ JsonSerializer, SerializerProvider }
import com.lvxingpai.yunkai.UserInfo

/**
 * Created by pengyt on 2015/11/17.
 */
class UserSerializer extends JsonSerializer[UserInfo] {

  override def serialize(user: UserInfo, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("userId", user.userId.toString)
    gen.writeStringField("nickname", user.nickName)
    gen.writeStringField("avatar", user.avatar.getOrElse(""))

    gen.writeEndObject()
  }
}
