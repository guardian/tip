import Dependencies._
import sbtrelease.ReleaseStateTransformations._

lazy val root = (project in file(".")).settings(
  name := "tip",
  description := "Scala library for testing in production.",
  organization := "com.gu",
  scalaVersion := "2.12.4",
  licenses := Seq("GPLv3" -> url("http://www.gnu.org/licenses/gpl.html")),
  scmInfo := Some(
    ScmInfo(url("https://github.com/guardian/tip"),
            "scm:git:git@github.com:guardian/tip.git")),
  libraryDependencies ++= dependencies,
  crossScalaVersions ++= Seq("2.11.12"),
  sources in doc in Compile := List(),
  releaseCrossBuild := true,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    releaseStepCommand("publishSigned"),
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    pushChanges
  ),
  (compile in Compile) := ((compile in Compile) dependsOn compileScalastyle).value,
  scalafmtTestOnCompile := true,
  scalafmtShowDiff := true
)

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")
compileScalastyle := scalastyle.in(Compile).toTask("").value
