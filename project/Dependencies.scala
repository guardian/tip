import sbt._

object Dependencies {
  val http4sVersion = "0.21.3"
  val akkaVersion = "2.6.4"

  lazy val test =       Seq("org.scalatest"               %%  "scalatest"           % "3.1.1"         % Test,
                            "org.mockito"                 %%  "mockito-scala"       % "1.13.9"         % Test)
  lazy val scalaLogging =   "com.typesafe.scala-logging"  %%  "scala-logging"       % "3.9.2"
  lazy val listJson =       "net.liftweb"                 %%  "lift-json"           % "3.4.1"
  lazy val ficus =          "com.iheart"                  %%  "ficus"               % "1.4.7"
  lazy val yaml =           "net.jcazevedo"               %%  "moultingyaml"        % "0.4.2"
  lazy val akka =       Seq("com.typesafe.akka"           %%  "akka-actor"          % akkaVersion,
                            "com.typesafe.akka"           %%  "akka-slf4j"          % akkaVersion)
  lazy val http4s =     Seq("org.http4s"                  %%  "http4s-dsl"          % http4sVersion,
                            "org.http4s"                  %%  "http4s-blaze-client" % http4sVersion,
                            "org.http4s"                  %%  "http4s-circe"        % http4sVersion)
  lazy val retry =      Seq("com.softwaremill.retry"      %% "retry"                % "0.3.3",
                            "com.softwaremill.odelay"     %% "odelay-core"          % "0.3.2")

  val all =
    Seq(
      scalaLogging,
      listJson,
      ficus,
      yaml
    )
    .++(http4s)
    .++(akka)
    .++(retry)
    .++(test)
}
