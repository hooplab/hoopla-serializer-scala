import org.scoverage.coveralls.Imports.CoverallsKeys._
import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform.ScalariformKeys

name := "serializer"

organization := "no.hoopla"

version := "1.0.0"

scalaVersion := "2.11.8"

scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

resolvers ++= Seq(
  Resolver.sonatypeRepo("releases")
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

libraryDependencies ++= {
  Seq(
    "org.scalaz" %% "scalaz-core" % "7.1.0",
    "org.scalatest" %% "scalatest" % "3.0.0-M1",
    "org.scalamock" %% "scalamock-scalatest-support" % "3.2.2",
    "org.scalaz" %% "scalaz-scalacheck-binding" % "7.1.0",
    "org.typelevel" %% "scalaz-scalatest" % "0.2.3",
    "org.json4s" %% "json4s-jackson" % "3.2.11"
  )
}

parallelExecution in Test := false

coverallsTokenFile := Some("coveralls-api-key")

scalacOptions ++= Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-unchecked",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ywarn-unused-import"
)

fork in Test := true

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 16)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true)
  .setPreference(AlignParameters, true)
  .setPreference(AlignArguments, true)
  .setPreference(SpacesAroundMultiImports, false)
