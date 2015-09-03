package no.hoopla.serializer

import org.json4s.JsonDSL._
import org.json4s._

object SimpleClasses {
  case object OrganizationSchema extends Schema {
    override def primaryKey = "organizationId"
    override def typeName = "organizations"
    override def attributes = List("name", "identifier")
  }
  case object UserSchema extends Schema {
    override def primaryKey = "userId"
    override def typeName = "users"
    override def attributes = List("name")
  }

  case class User(userId: String, name: String)
  case class Organization(organizationId: String, name: String, identifier: String)
}

import SimpleClasses._

class SimpleSerializerTest extends UnitSpec {
  it should "serialize a single simple object" in {

    val organization = Organization("1", "Brukbar", "brukbar")

    val serialized = Serializer.serialize(OrganizationSchema, organization)

    assert(serialized \ "id" == JString(organization.organizationId), "Primary keys should be equal")

    assertResult(serialized \ "attributes", "Attributes should be extracted") {
      ("name" -> organization.name) ~
      ("identifier"-> organization.identifier)
    }
  }

  it should "serialize a list of simple objects" in {

    val user1 = User("1", "Bob")
    val user2 = User("2", "Alice")
    val users = List(user1, user2)

    val serialized = Serializer.serialize(UserSchema, users)

    assertResult(serialized, "A list of objects should be serialized the same way as its elements") {
      JArray(List(Serializer.serialize(UserSchema, user1), Serializer.serialize(UserSchema, user2)))
    }
  }
}
