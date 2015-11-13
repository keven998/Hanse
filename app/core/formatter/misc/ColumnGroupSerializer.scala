package core.formatter.misc

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.{ SerializerProvider, JsonSerializer }
import core.model.misc.{ Column, ColumnGroup }

/**
 * Created by pengyt on 2015/11/13.
 */
class ColumnGroupSerializer extends JsonSerializer[ColumnGroup] {

  override def serialize(columnGroup: ColumnGroup, gen: JsonGenerator, serializers: SerializerProvider): Unit = {
    gen.writeStartObject()

    gen.writeStringField("columnType", columnGroup.columnType)

    // images
    gen.writeFieldName("columns")
    gen.writeStartArray()
    val columns = columnGroup.columns
    if (columns.nonEmpty) {
      val ret = serializers.findValueSerializer(classOf[Column], null)
      for (column <- columns)
        ret.serialize(column, gen, serializers)
    }
    gen.writeEndArray()

    gen.writeEndObject()
  }
}
