import sbt._

object Dependencies {
  lazy val scalaTest =      "org.scalatest"               %%  "scalatest"         % "3.0.1"  % Test
  lazy val logback =        "ch.qos.logback"              %   "logback-classic"   % "1.1.7"
  lazy val scalaLogging =   "com.typesafe.scala-logging"  %%  "scala-logging"     % "3.5.0"
  lazy val okhttp =         "com.squareup.okhttp3"        %   "okhttp"            % "3.6.0"
  lazy val listJson =       "net.liftweb"                 %%  "lift-json"         % "3.1.0-M1"
  lazy val ficus =          "com.iheart"                  %%  "ficus"             % "1.4.0"
  lazy val yaml =           "net.jcazevedo"               %%  "moultingyaml"      % "0.4.0"
  lazy val akkaActor =      "com.typesafe.akka"           %%  "akka-actor"        % "2.4.17"
  lazy val akkaLog =        "com.typesafe.akka"           %%  "akka-slf4j"        % "2.4.17"

  lazy val dependencies = Seq(scalaTest, logback, scalaLogging, okhttp, listJson, ficus, yaml, akkaActor, akkaLog)
}
