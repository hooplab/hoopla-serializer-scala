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

  def serialize(schema: SchemaBase, obj: Object): JValue = {
    val data = parse(write(obj))
    val (included, _) = addRelationships(List[JValue](), Map[String, Set[JValue]](), schema.relationships, data)

    ("data" -> serializeSchemaData(schema, data)) ~
    ("included" -> included)
  }

  private def addRelationships(included: List[JValue], visited: Map[String, Set[JValue]], relationships: List[Relationship], data: JValue):
      (List[JValue], Map[String, Set[JValue]]) =
    relationships.filter(_.included).foldLeft((included, visited)){case ((included, visited), relationship) =>
      addRelationship(included, visited, relationship.schema, data \ relationship.attribute)
    }

  private def addRelationship(included: List[JValue], visited: Map[String, Set[JValue]], relationshipSchema: SchemaBase, relationshipData: JValue):
      (List[JValue], Map[String, Set[JValue]]) =
    if (relationshipData.isInstanceOf[JArray]) {
      relationshipData.children.foldLeft(included, visited){case ((inc, vis), childData) =>
        addRelationship(inc, vis, relationshipSchema, childData)
      }}
      else {
        val typeName = relationshipSchema.typeName
        val primaryKey = relationshipData \ relationshipSchema.primaryKey

        // skip the relationship if already added
        if (visited.contains(typeName) && visited(typeName).contains(primaryKey))
          (included, visited)
        else {
          val newVisited = visited.updated(typeName, visited(typeName) + primaryKey)
          val newIncluded = serializeSchemaData(relationshipSchema, relationshipData) :: included
          addRelationships(newIncluded, newVisited, relationshipSchema.relationships, relationshipData)
        }
      }

  /**
    Every resource object MUST contain an id member and a type member. The values of the id and type members MUST be strings.
   */
  private def resourceIdentifierObject(schema: SchemaBase, data: JValue): JObject = {
    ("type" -> schema.typeName) ~
    ("id" -> JString(compact(render(data \ schema.primaryKey))))
  }

  private def relationship(rel: Relationship, data: JValue): JValue = {
    "data" -> relationshipData(rel.schema, data)
  }

  private def relationshipData(schema: SchemaBase, data: JValue): JValue = {
    data match {
      case JArray(_) => data.children.map(resourceIdentifierObject(schema, _))
      case JNothing => JNull
      case _ => resourceIdentifierObject(schema, data)
    }
  }

  private def serializeSchemaData(schema:SchemaBase, data: JValue): JValue = {
    data match {
      case JArray(_) => data.children.map(x => serializeSchemaData(schema, x))
      case JNothing => JNothing
      case _ =>
        resourceIdentifierObject(schema, data) ~
        ("attributes" -> schema.attributes.map(attr => attr -> data \ attr)) ~
        ("relationships" -> schema.relationships.map(rel => rel.attribute -> relationship(rel, data \ rel.attribute)))
    }
  }
}
