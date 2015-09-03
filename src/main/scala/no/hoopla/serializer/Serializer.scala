package no.hoopla.serializer

import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write

object Serializer {
  implicit val formats = Serialization.formats(NoTypeHints)

  def serialize(schema: Schema, obj: Object): JValue = {
    val data = parse(write(obj))

    serialize(schema, data)
  }

  private def serialize(schema:Schema, data: JValue): JValue = {
    if (data.isInstanceOf[JArray]) {
      data.children.map(x ⇒ serialize(schema, x))
    } else {

      ("type" -> schema.typeName) ~
      // TODO : id should always be of type JString
      // jsonapi: Every resource object MUST contain an id member and a type member. The values of the id and type members MUST be strings.
      ("id" -> data \ schema.primaryKey) ~
      ("attributes" -> schema.attributes.map(attr ⇒ attr -> data \ attr))
    }
  }
}
