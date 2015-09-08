name := "api"

organization := "no.hoopla"

version := "1.0.0"

scalaVersion := "2.11.7"

scalacOptions := Seq("-unchecked", "-feature", "-deprecation", "-encoding", "utf8")

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
