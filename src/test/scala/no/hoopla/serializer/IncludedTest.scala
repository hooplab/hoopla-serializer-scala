package no.hoopla.serializer

import no.hoopla.serializer.IncludedTestData._
import org.json4s._

object IncludedTestData {
  object PersonSchema extends Schema[Person] {
    override def primaryKey = "id"
    override def typeName = "persons"
    override def relationships = List(Relationship(PersonSchema, "boss", included=true))
  }
  case class Person(id: Long, boss: Option[Person])
}

class IncludedTest extends UnitSpec {

  private val torstein = Person(1, None)
  private val torkil = Person(2, Some(torstein))
  private val magnus = Person(3, Some(torkil))

  "An included resource" should "be added to 'included'" in {
    val serialized = Serializer.serialize(PersonSchema, magnus)

    val included = (serialized \ "included").children.filter(_.isInstanceOf[JObject])

    assert(included.exists(x => x \ "type" == JString("persons") && x \ "id" == JString(torkil.id.toString)))
    assert(included.exists(x => x \ "type" == JString("persons") && x \ "id" == JString(torstein.id.toString)))
    assert(!included.exists(x => x \ "type" == JString("persons") && x \ "id" == JString(magnus.id.toString)))
  }
}
