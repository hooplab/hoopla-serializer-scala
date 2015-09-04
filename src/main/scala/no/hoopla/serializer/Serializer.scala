package no.hoopla.serializer

import org.json4s.JsonAST.JNothing
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write

import scala.collection.mutable

object Serializer {
  implicit val formats = Serialization.formats(NoTypeHints)

  class Serializer {
    private var included = List[JValue]()
    private var visited = Map[String, mutable.Set[JValue]]()

    def serialize(schema: Schema, data: JValue): JValue = {
      schema.included.foreach(include => {
        addIncluded(include.schema, data \ include.attribute)
      })

      ("data" -> serializeSchemaData(schema, data)) ~
      ("included" -> included)
    }

    private def addIncluded(schema: Schema, data: JValue): Unit = {
      if (data.isInstanceOf[JArray]) {
        data.children.foreach(x ⇒ addIncluded(schema, x))
        return
      }

      val typeName = schema.typeName
      val primaryKey = data \ schema.primaryKey

      if (!visited.contains(typeName))
        visited += typeName -> mutable.Set()

      if (visited(typeName).contains(primaryKey))
        return

      visited(typeName) += primaryKey

      included ::= serializeSchemaData(schema, data)

      schema.included.foreach(include => {
        addIncluded(include.schema, data \ include.attribute)
      })
    }
  }
  def serialize(schema: Schema, obj: Object): JValue = {
    val data = parse(write(obj))

    new Serializer().serialize(schema, data)
  }


  private def resourceIdentifierObject(schema: Schema, data: JValue): JObject = {
    ("type" -> schema.typeName) ~
    // TODO : id should always be of type JString
    // jsonapi: Every resource object MUST contain an id member and a type member. The values of the id and type members MUST be strings.
    ("id" -> data \ schema.primaryKey)
  }

  private def relationship(rel: Relationship, data: JValue): JValue = {
    "data" -> relationshipData(rel.schema, data)
  }

  private def relationshipData(schema: Schema, data: JValue): JValue = {
    data match {
      case JArray(_) => data.children.map(resourceIdentifierObject(schema, _))
      case JNothing => JNull
      case _ => resourceIdentifierObject(schema, data)
    }
  }

  private def serializeSchemaData(schema:Schema, data: JValue): JValue = {
    data match {
      case JArray(_) => data.children.map(x ⇒ serializeSchemaData(schema, x))
      case JNothing => JNothing
      case _ =>
        resourceIdentifierObject(schema, data) ~
        ("attributes" -> schema.attributes.map(attr ⇒ attr -> data \ attr)) ~
        ("relationships" -> (schema.relationships union schema.included).map(rel ⇒ rel.attribute -> relationship(rel, data \ rel.attribute)))
    }
  }
}
