package no.hoopla.serializer

class SchemaValidationTest extends UnitSpec {

  // BlankSchema is a valid schema used for testing references
  private[this] case class Blank(id: String)
  private[this] object BlankSchema extends Schema[Blank] {
    override def primaryKey: String = "id"
    override def typeName: String = ""
  }

  private[this] case class Person(id: String)

  // Schema matching Person. Overriding in tests to create exceptions
  private[this] abstract class CorrectPersonSchema extends Schema[Person] {
    override def primaryKey = "id"
    override def typeName = "persons"
    override def attributes = List[String]()
    override def relationships  = List[Relationship]()
  }

  /*
   * Objects in scala are lazy, and are not initialized until the first reference.
   * http://stackoverflow.com/questions/6249569/force-initialization-of-scala-singleton-object
   *
   * We need to reference the schemas (val a: Schema[T] = ...) to call the Schema[T] constructor for validation
   */
  "MissingFieldsException" should "be thrown if schema has mismatching primary key" in {
    val mismatchingPrimaryKey = "dogId"

    val thrown = intercept[MissingFieldsException] {
      object PersonSchema extends CorrectPersonSchema {
        override def primaryKey: String = mismatchingPrimaryKey
      }
      val a = PersonSchema
    }

    assert(thrown.missingFields == List(mismatchingPrimaryKey))
  }

  it should "be thrown if schema has mismatching attributes" in {
    val missingAttributes = List("a", "b", "c")

    val thrown = intercept[MissingFieldsException] {
      object PersonSchema extends CorrectPersonSchema {
        override def attributes = missingAttributes
      }
      val a = PersonSchema
    }

    assert(thrown.missingFields == missingAttributes)
  }

  it should "be thrown if schema relationships has mismatching attributes" in {
    val missingRelationshipAttributes = List("a", "b", "c")

    val thrown = intercept[MissingFieldsException] {
      object PersonSchema extends CorrectPersonSchema {
        override def relationships = missingRelationshipAttributes.map(Relationship(BlankSchema, _))
      }
      val a = PersonSchema
    }

    assert(thrown.missingFields == missingRelationshipAttributes)
  }

  it should "be thrown and contain all missing fields (primaryKey, attributes, relationships.attribute)" in {
    val mismatchingPrimaryKey = "dogId"
    val missingAttributes = List("a", "b", "c")
    val missingRelationshipAttributes = List("d", "e", "f")

    val missingFields = mismatchingPrimaryKey :: missingAttributes ::: missingRelationshipAttributes

    val thrown = intercept[MissingFieldsException] {
      object PersonSchema extends CorrectPersonSchema {
        override def primaryKey = mismatchingPrimaryKey
        override def attributes = missingAttributes
        override def relationships = missingRelationshipAttributes.map(Relationship(BlankSchema, _))
      }
      val a = PersonSchema
    }

    // Casting to set since the ordering does not matter
    assert(thrown.missingFields.toSet == missingFields.toSet)
  }
}
