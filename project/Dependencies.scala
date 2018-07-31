import sbt._

object Dependencies {
  val http4sVersion = "0.18.15"
  val akkaVersion = "2.5.8"

  lazy val scalaTest =      "org.scalatest"               %%  "scalatest"           % "3.0.4"         % Test
  lazy val scalaLogging =   "com.typesafe.scala-logging"  %%  "scala-logging"       % "3.8.0"
  lazy val listJson =       "net.liftweb"                 %%  "lift-json"           % "3.3.0"
  lazy val ficus =          "com.iheart"                  %%  "ficus"               % "1.4.3"
  lazy val yaml =           "net.jcazevedo"               %%  "moultingyaml"        % "0.4.0"
  lazy val akka =       Seq("com.typesafe.akka"           %%  "akka-actor"          % akkaVersion,
                            "com.typesafe.akka"           %%  "akka-slf4j"          % akkaVersion)
  lazy val http4s =     Seq("org.http4s"                  %%  "http4s-dsl"          % http4sVersion,
                            "org.http4s"                  %%  "http4s-blaze-client" % http4sVersion,
                            "org.http4s"                  %%  "http4s-circe"        % http4sVersion)

  lazy val dependencies =
    Seq(
      scalaTest,
      scalaLogging,
      listJson,
      ficus,
      yaml)
    .++(http4s)
    .++(akka)
}
