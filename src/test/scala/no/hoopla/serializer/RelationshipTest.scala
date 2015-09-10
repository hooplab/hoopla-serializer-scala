package no.hoopla.serializer

import no.hoopla.serializer.RelationshipTestData._
import org.json4s.JsonAST.{JArray, JNull, JString, JObject}

object RelationshipTestData {
  object PersonSchema extends Schema[Person] {
    override def primaryKey = "id"
    override def typeName = "persons"
    override def relationships = List(Relationship(PersonSchema, "boss"))
  }

  case class Person(id: Long, boss: Option[Person])

  object ReproductivePersonSchema extends Schema[ReproductivePerson] {
    override def primaryKey = "id"
    override def typeName = "reproductivePersons"
    override def relationships = List(Relationship(ReproductivePersonSchema, "children"))
  }

  case class ReproductivePerson(id: Long, children: List[ReproductivePerson])

  object ReproductivePersonSchema2 extends Schema[ReproductivePerson2] {
    override def primaryKey = "id"
    override def typeName = "reproductivePersons"
    override def relationships = List(Relationship(ReproductivePersonSchema2, "children"))
  }

  case class ReproductivePerson2(id: Long, children: List[Option[ReproductivePerson2]])
}

class RelationshipTest extends UnitSpec {
  private[this] val boss = Person(1, None)
  private[this] val personWithBoss = Person(2, Some(boss))

  "A relationship" should "be null if the optional value is None" in {
    val serialized = Serializer.serialize(PersonSchema, boss)

    (serialized \ "data" \ "relationships" \ "boss" \ "data") shouldBe JNull
  }

  it should "be added to 'relationships'" in {
    val serialized = Serializer.serialize(PersonSchema, personWithBoss)

    assertResult(JString(boss.id.toString)) {
      serialized \ "data" \ "relationships" \ "boss" \ "data" \ "id"
    }
  }

  it should "not be added to 'included' when inluded=false" in {
    val serialized = Serializer.serialize(PersonSchema, personWithBoss)

    (serialized \ "included") shouldBe JArray(List())
  }

  val childlessCharles = ReproductivePerson(1, Nil)
  "Multiple relationships" should "yield an empty list if there are no relationships" in {
    val serialized = Serializer.serialize(ReproductivePersonSchema, childlessCharles)

    assertResult(JArray(Nil)) {
      serialized \ "data" \ "relationships" \ "children" \ "data"
    }
  }

  it should "yield a flattened list with no null objects" in {
    val childlessClarence = ReproductivePerson2(2, List(None, None, None, None))
    val serialized = Serializer.serialize(ReproductivePersonSchema2, childlessClarence)

    assertResult(JArray(Nil)) {
      serialized \ "data" \ "relationships" \ "children" \ "data"
    }
  }

  it should "be added to 'relationships'" in {
    val fertileFrank = ReproductivePerson(3, List(childlessCharles))
    val serialized = Serializer.serialize(ReproductivePersonSchema, fertileFrank)

    val relationships = (serialized \ "data" \ "relationships" \ "children" \ "data").children.filter(_.isInstanceOf[JObject])

    assert(relationships.exists(x => x \ "type" == JString("reproductivePersons") && x \ "id" == JString(childlessCharles.id.toString)))
  }
}
