package no.hoopla.serializer

import scala.reflect.runtime.universe._

abstract class Schema[T: TypeTag] extends SchemaBase {
  def fieldIsOfType(field: String, t: Type): Boolean = {
    typeOf[T].member(TermName(field)).typeSignature.resultType == t
  }

  private val missingAttrs = (primaryKey :: attributes ::: relationships.map(_.attribute)).filter(memberIsOfType(_, NoType))
  if (missingAttrs.nonEmpty) {
    throw new RuntimeException(s"Class ${typeOf[T]} is missing the following fields: ${missingAttrs.mkString(", ")}")
  }
}

sealed trait SchemaBase {
  def primaryKey: String
  def typeName: String
  def attributes: List[String] = List()
  def relationships: List[Relationship] = List()
}

case class Relationship(schema: SchemaBase, attribute: String, included: Boolean = false)
