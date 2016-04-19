package no.hoopla.serializer

class SchemaMacroValidationTest extends UnitSpec {

  // BlankSchema is a valid schema used for testing references
  private[this] case class Blank(id: String)

  private[this] case class Person(id: String)

  // Schema matching Person. Overriding in tests to create exceptions
  private[this] abstract class CorrectPersonSchema extends Schema[Person] {
    override def primaryKey: String = "id"
    override def typeName: String = "persons"
    override def attributes: List[String] = List()
    override def relationships: List[Relationship] = List()
  }

  "this file" should "compile" in {
    @ValidateSchema
    case object BlankSchema extends Schema[Blank] {
      override def primaryKey: String = "id"
      override def typeName: String = "typeName"
      override def attributes: List[String] = List("fjell")
    }
  }
}
