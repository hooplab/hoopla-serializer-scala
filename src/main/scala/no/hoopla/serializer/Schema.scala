package no.hoopla.serializer

trait Schema {
  def primaryKey: String
  def typeName: String
  def attributes: List[String] = List()
  def relationships: List[Relationship] = List()
  def included: List[Relationship] = List()
}

case class Relationship(schema: Schema, attribute: String)
