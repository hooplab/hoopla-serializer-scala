package no.hoopla.serializer

import org.json4s.JsonAST.{JArray, JInt, JNull}

object RelationshipObjects {
  case object PersonSchema extends Schema {
    override def primaryKey = "id"
    override def typeName = "persons"
    override def relationships = List(Relationship(PersonSchema, "boss"))
  }

  case class Person(id: Int, boss: Option[Person])
}

import no.hoopla.serializer.RelationshipObjects._

class SimpleRelationshipTest extends UnitSpec {
  private val boss = Person(1, None)
  private val personWithBoss = Person(1, Some(boss))
  
  "A relationship" should "be null if the optional value is None" in {
    val serialized = Serializer.serialize(PersonSchema, boss)

    (serialized \ "data" \ "relationships" \ "boss" \ "data") shouldBe JNull
  }

  it should "be added to 'relationships'" in {
    val serialized = Serializer.serialize(PersonSchema, personWithBoss)

    assertResult(JInt(boss.id)) {
      serialized \ "data" \ "relationships" \ "boss" \ "data" \ "id"
    }
  }

  it should "not be added to 'included'" in {
    val serialized = Serializer.serialize(PersonSchema, personWithBoss)

    (serialized \ "included") shouldBe JArray(List())
  }
}
