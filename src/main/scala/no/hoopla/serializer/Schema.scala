package no.hoopla.serializer

import scala.reflect.runtime.universe._

abstract class Schema[T: TypeTag] extends SchemaBase {
  def fieldIsOfType(field: String, t: Type): Boolean = {
    typeOf[T].member(TermName(field)).typeSignature.resultType == t
  }

  private val missingFields = (primaryKey :: attributes ::: relationships.map(_.attribute)).filter(fieldIsOfType(_, NoType))
  if (missingFields.nonEmpty) {
    throw new NoSuchFieldException(s"Class ${typeOf[T]} is missing the following fields: ${missingFields.mkString(", ")}")
  }
}

sealed trait SchemaBase {
  def primaryKey: String
  def typeName: String
  def attributes: List[String] = List()
  def relationships: List[Relationship] = List()
}

case class Relationship(schema: SchemaBase, attribute: String, included: Boolean = false)
