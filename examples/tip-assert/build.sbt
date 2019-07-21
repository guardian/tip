ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

val circeVersion = "0.12.0-M4"

lazy val root = (project in file("."))
  .settings(
    name := "tip-assert",
    libraryDependencies ++= Seq(
      "com.gu" %% "tip" % "0.6.1", 
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
      "org.scalaj" %% "scalaj-http" % "2.4.2",
    ),
      libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser",
      "io.circe" %% "circe-generic-extras",
    ).map(_ % circeVersion),
  )
