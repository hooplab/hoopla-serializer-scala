package no.hoopla.serializer

import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._
import org.json4s.jackson.Serialization
import org.json4s.jackson.Serialization.write

object Serializer {
  private[this] type Visited = Map[String, Set[JValue]]
  private[this] type Included = List[JValue]
  private[this] type IncludedVisited = (List[JValue], Visited)

  private[this] implicit val formats: Formats = Serialization.formats(NoTypeHints)

  def serialize[T <: AnyRef](schema: Schema[T], obj: T): JValue = {
    toJsonApi(schema, obj)
  }

  def serialize[T <: AnyRef](schema: Schema[T], iterable: Iterable[T]): JValue = {
    toJsonApi(schema, iterable)
  }

  private[this] def toJsonApi(schema: SchemaBase, obj: AnyRef): JValue = {
    val data = parse(write(obj))
    val (included, _) = addAllIncluded(List[JValue](), Map[String, Set[JValue]](), schema.relationships, data)

    ("data" -> serializeSchemaData(schema, data)) ~
    ("included" -> included)
  }

  private[this] def addAllIncluded(included: Included, visited: Visited, relationships: List[Relationship], data: JValue): IncludedVisited =
    relationships.filter(_.included).foldLeft((included, visited)){ case ((inc, vis), relationship) =>
      addIncluded(inc, vis, relationship.schema, data \ relationship.attribute)
    }

  private[this] def addIncluded(included: Included, visited: Visited, relationshipSchema: SchemaBase, relationshipData: JValue): IncludedVisited =
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
  private[this] def resourceIdentifierObject(schema: SchemaBase, data: JValue): JObject =
    ("type" -> schema.typeName) ~
    ("id" -> JString(compact(render(data \ schema.primaryKey))))


  private[this] def relationship(rel: Relationship, data: JValue): JValue =
    "data" -> relationshipData(rel.schema, data)


  private[this] def relationshipData(schema: SchemaBase, data: JValue): JValue =
    data match {
      case JArray(_) => data.children.map(resourceIdentifierObject(schema, _))
      case JNothing => JNull
      case _ => resourceIdentifierObject(schema, data)
    }

  private[this] def serializeSchemaData(schema:SchemaBase, data: JValue): JValue =
    data match {
      case JArray(_) => data.children.map(x => serializeSchemaData(schema, x))
      case JNothing => JNothing
      case _ =>
        resourceIdentifierObject(schema, data) ~
        ("attributes" -> schema.attributes.map(attr => attr -> data \ attr)) ~
        ("relationships" -> schema.relationships.map(rel => rel.attribute -> relationship(rel, data \ rel.attribute)))
    }
}
