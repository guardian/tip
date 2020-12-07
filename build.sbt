import sbtrelease.ReleaseStateTransformations._

lazy val root = (project in file(".")).settings(
  name := "tip",
  description := "Scala library for testing in production.",
  organization := "com.gu",
  scalaVersion := "2.13.4",
  libraryDependencies ++= Dependencies.all,
  sources in doc in Compile := List(),
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
  scalacOptions ++= Seq(
    "-Ywarn-unused:imports",
    "-Yrangepos"
  ),
  addCompilerPlugin(scalafixSemanticdb)
)

lazy val compileScalastyle = taskKey[Unit]("compileScalastyle")
compileScalastyle := scalastyle.in(Compile).toTask("").value
