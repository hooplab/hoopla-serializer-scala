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

  case object UserSchema extends Schema[User] {
    override def primaryKey: String = "userId"
    override def typeName: String = "users"
  }
  object OrganizationSchema extends Schema[Organization] {
    override def primaryKey = "organizationId"
    override def typeName = "organizations"
    override def relationships = List(
      Relationship(UserSchema, "admin", included=true),
      Relationship(UserSchema, "members", included=true)
    )
  }
  case class User(userId: Int)
  case class Organization(organizationId: Int, admin: User, members: List[User])
}

class IncludedTest extends UnitSpec {
  private[this] def identifiesAs(typeName: String, id: Any)(x:JValue): Boolean = {
    x \ "type" == JString(typeName) && x \ "id" == JString(id.toString)
  }

  "An included resource" should "be added to 'included'" in {
    val torstein = Person(1, None)
    val torkil = Person(2, Some(torstein))
    val magnus = Person(3, Some(torkil))

    val serialized = Serializer.serialize(PersonSchema, magnus)

    val included = (serialized \ "included").children.filter(_.isInstanceOf[JObject])

    assertResult(false, "Should not include self")(
      included.exists(identifiesAs("persons", magnus.id))
    )
    assert(included.exists(identifiesAs("persons", torkil.id)))
    assert(included.exists(identifiesAs("persons", torstein.id)))
  }

  "Included resources with same type and id" should "only be added once in 'included'" in {
    val admin = User(1)
    val users = admin :: User(2) :: Nil
    val organization = Organization(1, admin, users)

    val serialized = Serializer.serialize(OrganizationSchema, organization)

    val included = (serialized \ "included").children.filter(_.isInstanceOf[JObject])

    assertResult(1, "Admin should only be added once")(
      included.count(identifiesAs("users", admin.userId))
    )
  }
}
