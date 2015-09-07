package no.hoopla.serializer

import scala.reflect.runtime.universe._

abstract class Schema[T: TypeTag] extends SchemaBase {
  def memberIsOfType(member: String, `type`: Type): Boolean = {
    typeOf[T].member(TermName(member)).typeSignature.resultType == `type`
  }

  if (!memberIsOfType(primaryKey, typeOf[Long])) {
    throw new RuntimeException(s"Primary key ('$primaryKey') must be long in class ${typeOf[T]}")
  }

  private val missingAttrs = (relationships.map(_.attribute) ::: attributes).filter(memberIsOfType(_, NoType))
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
