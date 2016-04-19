package no.hoopla.serializer

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.reflect.macros.whitebox.Context
import scala.collection.mutable.{ListBuffer, Stack}
import scala.language.experimental.macros

@compileTimeOnly("enable macro paradise to expand macro annotations.")
class ValidateSerializable extends StaticAnnotation {
  def macroTransform(annottees: Any*): Any = macro ???

}

object ValidateSerializable {
  def impl(c: Context)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    //def impl(c: CrossVersionContext)(annottees: c.Expr[Any]*): c.Expr[Any] = {
    //import c.universe._

    //annottees.map(_.tree) match {
    //  case _ => c.abort(c.enclosingPosition, "Invalid annottee")
    //}
    ???
  }
}

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
      var relationships: List[Relationship] = List()

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

        case _ => super.traverse(tree)
      }
    }

    annottees.map(_.tree) match {
      case (objectDecl: ModuleDef) :: Nil => {
        traverser.traverse(objectDecl)
        println(traverser.baseTypeName)
        println(traverser.primaryKeyName)
        println(traverser.typeName)
        println(traverser.attributes)
        println(traverser.relationships)

        ???
      }

      case something => {
        println(something)
        c.abort(c.enclosingPosition, "Invalid annottee, is your schema a case object?")
      }
    }
  }
}
