import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "com.gu",
      scalaVersion := "2.12.1",
      version      := "0.1.0-SNAPSHOT"
    )),
    name := "tip",
    crossScalaVersions ++= Seq("2.11.2"),
    libraryDependencies ++= Seq(
      scalaTest % Test,
      "ch.qos.logback"              %   "logback-classic"   % "1.1.7",
      "com.typesafe.scala-logging"  %%  "scala-logging"     % "3.5.0",
      "com.squareup.okhttp3"        %   "okhttp"            % "3.6.0",
      "net.liftweb"                 %%  "lift-json"         % "3.1.0-M1",
      "com.iheart"                  %%  "ficus"             % "1.4.0"
    )
  )
