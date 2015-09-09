hoopla-serializer-scala
=========================
[![Codacy Badge](https://api.codacy.com/project/badge/bb7db5d6e29c4b789c2b8eee180f774a)](https://www.codacy.com/app/hooplab/hoopla-serializer-scala)

[![Build Status](https://ci.hoopladev.no/buildStatus/icon?job=hoopla-serializer-scala)](https://ci.hoopladev.no/job/hoopla-serializer-scala/)

[![Coverage Status](https://coveralls.io/repos/hooplab/hoopla-serializer-scala/badge.svg?branch=a030deb0822a1d3bf5cabd06258e5483ee904f1e&service=github)](https://coveralls.io/github/hooplab/hoopla-serializer-scala?branch=a030deb0822a1d3bf5cabd06258e5483ee904f1e)


## jsonapi
hoopla-serializer-scala is a jsonapi.org serializer, given a json4s format it will produce valid json4s output.

## Structure
no.hoopla.serializer.Schema describes how to convert an object into jsonapi output,
no.hoopla.serializer.Serializer contains one public function (serialize) that serializes based on a Schema and an object.

## API
A schema is an abstract class with the following fields
```Scala
// the name of the primary key
primaryKey: String

// the name of the type to serialize
typeName: String

// names of attributes or fields in the class to include, defaults to empty
attributes: List[String] = List()

// relationships to include, defaults to empty
relationships: List[Relationship] = List()
```
A relationship is a case class for describing how schemas interact, containing the following fields:
```Scala
// the schema of the related resource
schema: Schema

// the field in the model that is serialized with this relationship's schema.
attribute: String

// whether to include the data in the output, defaults to false.
included: Boolean = false
```
## Example Usage
```Scala
import org.json4s._
import org.json4s.jackson.JsonMethods._
import no.hoopla.serializer._

object SerializationExample extends App {
  // the case class to serialize
  case class Person(id: Long, name: String, supervisor: Option[Person])

  // the schema
  object PersonSchema extends Schema[Person] {
    override def primaryKey = "id"
    override def typeName = "persons"
    // include name in the json output.
    override def attributes = List("name")
    // a Person may have a boss, include it in output
    override def relationships = List(Relationship(PersonSchema, "supervisor", included=true))
  }

  // the data
  val executiveErica = Person(3, "Erica the Eldricht", None)
  val middleManagementMark = Person(2, "Mark the Magniloquent", Some(executiveErica))
  val builderBob = Person(1, "Bob the Bacciferous", Some(middleManagementMark))

  // serialize!
  val jsonOutput: JValue = Serializer.serialize(PersonSchema, builderBob)

  // pretty print output
  println(pretty(render(jsonOutput)))
}
```
Expected output:
```JavaScript
{
  "data" : {
    "type" : "persons",
    "id" : "1",
    "attributes" : {
      "name" : "Bob the Bacciferous"
    },
    "relationships" : {
      "supervisor" : {
        "data" : {
          "type" : "persons",
          "id" : "2"
        }
      }
    }
  },
  "included" : [ {
    "type" : "persons",
    "id" : "3",
    "attributes" : {
      "name" : "Erica the Eldricht"
    },
    "relationships" : {
      "supervisor" : {
        "data" : null
      }
    }
  }, {
    "type" : "persons",
    "id" : "2",
    "attributes" : {
      "name" : "Mark the Magniloquent"
    },
    "relationships" : {
      "supervisor" : {
        "data" : {
          "type" : "persons",
          "id" : "3"
        }
      }
    }
  } ]
}
```
For more usage, see the tests.