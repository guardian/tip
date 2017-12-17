import sbt._

object Dependencies {
  lazy val scalaTest =      "org.scalatest"               %%  "scalatest"         % "3.0.4"  % Test
  lazy val logback =        "ch.qos.logback"              %   "logback-classic"   % "1.2.3"
  lazy val scalaLogging =   "com.typesafe.scala-logging"  %%  "scala-logging"     % "3.7.2"
  lazy val okhttp =         "com.squareup.okhttp3"        %   "okhttp"            % "3.9.1"
  lazy val listJson =       "net.liftweb"                 %%  "lift-json"         % "3.1.1"
  lazy val ficus =          "com.iheart"                  %%  "ficus"             % "1.4.3"
  lazy val yaml =           "net.jcazevedo"               %%  "moultingyaml"      % "0.4.0"
  lazy val akkaActor =      "com.typesafe.akka"           %%  "akka-actor"        % "2.5.8"
  lazy val akkaLog =        "com.typesafe.akka"           %%  "akka-slf4j"        % "2.5.8"

  lazy val dependencies = Seq(scalaTest, logback, scalaLogging, okhttp, listJson, ficus, yaml, akkaActor, akkaLog)
}
