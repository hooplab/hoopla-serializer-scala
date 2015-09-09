package no.hoopla.serializer

import scala.reflect.runtime.universe._

abstract class InvalidSchemaException(message: String) extends RuntimeException(message)

case class MissingFieldsException(t: Type, missingFields: List[String]) extends InvalidSchemaException(
  s"Class $t is missing the following fields: ${missingFields.mkString(", ")}"
)

abstract class Schema[T: TypeTag] extends SchemaBase {
  private def validate(): Unit = {
    def fieldIsMissingInModel(field: String): Boolean =
      typeOf[T].member(TermName(field)).typeSignature.resultType == NoType

    val fields = primaryKey :: attributes ::: relationships.map(_.attribute)
    val missingFields = fields.filter(fieldIsMissingInModel)

    if (missingFields.nonEmpty) {
      throw MissingFieldsException(typeOf[T], missingFields)
    }
  }
  validate()
}

sealed trait SchemaBase {
  def primaryKey: String
  def typeName: String
  def attributes: List[String] = List()
  def relationships: List[Relationship] = List()
}

case class Relationship(schema: SchemaBase, attribute: String, included: Boolean = false)
