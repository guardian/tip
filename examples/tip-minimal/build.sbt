ThisBuild / scalaVersion     := "2.12.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"

lazy val root = (project in file("."))
  .settings(
    name := "tip-minimal",
    libraryDependencies ++= Seq(
      "com.gu" %% "tip" % "0.6.1", 
      "ch.qos.logback" % "logback-classic" % "1.2.3",
       "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
    )
  )
