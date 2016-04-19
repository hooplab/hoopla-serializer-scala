package no.hoopla.serializer

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.reflect.macros.whitebox.Context
import scala.language.experimental.macros

@compileTimeOnly("enable macro paradise to expand macro annotations.")
class ValidateSchema extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ValidateSchema.impl

}

object ValidateSchema {
  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    import c.universe._

    object traverser extends Traverser {
      var baseTypeName: String = ""
      var primaryKeyName: String = ""
      var typeName: String = ""
      var attributes: List[String] = List()
      var relationshipAttributes: List[String] = List()

      override def traverse(tree: Tree): Unit = tree match {
        case AppliedTypeTree(Ident(_), List(Ident(TypeName(btn)))) =>
          baseTypeName = btn
          super.traverse(tree)

        case DefDef(Modifiers(_), TermName("typeName"), _, _, Ident(_), Literal(Constant(tn))) =>
          typeName = tn.asInstanceOf[String]
          super.traverse(tree)

        case DefDef(Modifiers(_), TermName("primaryKey"), _, _, Ident(_), Literal(Constant(pk))) =>
          primaryKeyName = pk.asInstanceOf[String]
          super.traverse(tree)

        case DefDef(Modifiers(_), TermName("attributes"), _, _, _, Apply(_, attributeList)) =>
          attributes = attributeList.map {
            case Literal(Constant(attribute: String)) => attribute
            case _ => c.abort(c.enclosingPosition, "unable to parse attribute list. Use Constant Strings.")
          }
          super.traverse(tree)

        case DefDef(Modifiers(_), TermName("relationships"), _, _, _, Apply(_, relationshipList)) =>
          relationshipAttributes = relationshipList.map {
            case Apply(_, List(_, Literal(Constant(relationshipAttribute: String)))) =>
              relationshipAttribute

            case Apply(_, List(_, Literal(Constant(relationshipAttribute: String)), _)) =>
              relationshipAttribute

            case t =>
              c.abort(c.enclosingPosition, s"unable to parse relationship list. Use constant Strings. see: ${showRaw(t)}")
          }
          super.traverse(tree)

        case _ => super.traverse(tree)
      }
    }
    annottees.map(_.tree) match {
      case (objectDecl: ModuleDef) :: Nil => {
        //println(showRaw(objectDecl))
        traverser.traverse(objectDecl)

        val baseTypeName: String = traverser.baseTypeName
        val primaryKeyName: String = traverser.primaryKeyName
        val typeName: String = traverser.typeName
        val attributes: List[String] = traverser.attributes
        val relationships: List[String] = traverser.relationshipAttributes

        val requiredAttributes: Set[String] =
          attributes.toSet ++ relationships.toSet + primaryKeyName

        val k = c.asInstanceOf[scala.reflect.macros.contexts.Context]
        locally {
          import k.universe._
          val n = k.callsiteTyper.typed(q"??? : ${TypeName(traverser.baseTypeName)}").tpe
          println(n)
          requiredAttributes.foreach(attribute =>
            n.member(TermName(attribute)) match {
              case NoSymbol => c.abort(c.enclosingPosition, s"Can't find attribute $attribute in $n")
              case _        => ()
            })

          println(n)
        }
        ???
      }

      case t => {
        c.abort(c.enclosingPosition, s"Invalid annottee, is your schema a case object?, see: ${showRaw(t)}")
      }
    }
  }
}
