package no.hoopla.serializer

import no.hoopla.serializer.SimpleSerializerTestData._
import org.json4s.JsonDSL._
import org.json4s._

object SimpleSerializerTestData {
  case object OrganizationSchema extends Schema[Organization] {
    override def primaryKey: String = "organizationId"
    override def typeName: String = "organizations"
    override def attributes: List[String] = List("name", "identifier")
  }
  case object UserSchema extends Schema[User] {
    override def primaryKey: String = "userId"
    override def typeName: String = "users"
    override def attributes: List[String] = List("name")
  }

  case class User(userId: Long, name: String)
  case class Organization(organizationId: Long, name: String, identifier: String)
}

class SimpleSerializerTest extends UnitSpec {

  it should "serialize a single simple object" in {
    val organization = Organization(1, "Brukbar", "brukbar")

    val serialized = Serializer.serialize(OrganizationSchema, organization) \ "data"

    assert(serialized \ "id" == JString(organization.organizationId.toString), "Primary keys should be equal")

    assertResult(serialized \ "attributes", "Attributes should be extracted") {
      ("name" -> organization.name) ~
      ("identifier"-> organization.identifier)
    }
  }

  it should "serialize a list of simple objects" in {

    val user1 = User(1, "Bob")
    val user2 = User(2, "Alice")
    val users = List(user1, user2)

    val serialized = Serializer.serialize(UserSchema, users) \ "data"

    assertResult(serialized, "A list of objects should be serialized the same way as its elements") {
      JArray(List(Serializer.serialize(UserSchema, user1) \ "data", Serializer.serialize(UserSchema, user2) \ "data"))
    }
  }
}
