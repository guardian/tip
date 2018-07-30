import Dependencies._
import sbtrelease.ReleaseStateTransformations._

lazy val root = (project in file(".")).settings(
  name := "tip",
  description := "Scala library for testing in production.",
  organization := "com.gu",
  scalaVersion := "2.12.6",
  libraryDependencies ++= dependencies,
  sources in doc in Compile := List(),
  publishTo := Some(
    if (isSnapshot.value) { Opts.resolver.sonatypeSnapshots }
    else { Opts.resolver.sonatypeReleases }
  ),
  releasePublishArtifactsAction := PgpKeys.publishSigned.value,
  releaseProcess := Seq[ReleaseStep](
    checkSnapshotDependencies,
    inquireVersions,
    runClean,
    runTest,
    setReleaseVersion,
    commitReleaseVersion,
    tagRelease,
    publishArtifacts,
    setNextVersion,
    commitNextVersion,
    releaseStepCommand("sonatypeReleaseAll"),
    pushChanges
  ),
  (compile in Compile) := ((compile in Compile) dependsOn compileScalastyle).value,
  scalafmtTestOnCompile := true,
  scalafmtShowDiff := true,
  scalacOptions += "-Ywarn-unused-import",
  scalafixSettings
)

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")
compileScalastyle := scalastyle.in(Compile).toTask("").value
