package no.hoopla.serializer

import org.json4s.JsonAST.JNothing
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write

object Serializer {
  implicit val formats = Serialization.formats(NoTypeHints)

  type Visited = Map[String, Set[JValue]]
  type Included = List[JValue]
  type IncludedVisited = (List[JValue], Visited)

  def serialize(schema: SchemaBase, obj: Object): JValue = {
    val data = parse(write(obj))
    val (included, _) = addAllIncluded(List[JValue](), Map[String, Set[JValue]](), schema.relationships, data)

    ("data" -> serializeSchemaData(schema, data)) ~
    ("included" -> included)
  }

  private def addAllIncluded(included: Included, visited: Visited, relationships: List[Relationship], data: JValue): IncludedVisited =
    relationships.filter(_.included).foldLeft((included, visited)){ case ((included, visited), relationship) =>
      addIncluded(included, visited, relationship.schema, data \ relationship.attribute)
    }

  private def addIncluded(included: Included, visited: Visited, relationshipSchema: SchemaBase, relationshipData: JValue): IncludedVisited =
    if (relationshipData.isInstanceOf[JArray]) {
      relationshipData.children.foldLeft(included, visited){ case ((inc, vis), childData) =>
        addIncluded(inc, vis, relationshipSchema, childData)
      }
    } else {
      val typeName = relationshipSchema.typeName
      val primaryKey = relationshipData \ relationshipSchema.primaryKey

      val visitedPrimaryKeys = visited.getOrElse(typeName, Set[JValue]())

      if (visitedPrimaryKeys.contains(primaryKey)) {
        (included, visited)
      } else {
        addAllIncluded(
          serializeSchemaData(relationshipSchema, relationshipData) :: included,
          visited.updated(typeName, visitedPrimaryKeys + primaryKey),
          relationshipSchema.relationships,
          relationshipData
        )
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
