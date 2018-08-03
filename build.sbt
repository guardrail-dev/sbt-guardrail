sbtPlugin := true

name := "sbt-guardrail"
organization := "com.twilio"
description := "Principled code generation for Scala services from OpenAPI specifications, sbt plugin"
homepage := Some(url("https://github.com/twilio/sbt-guardrail"))
licenses += ("MIT", url("https://github.com/twilio/guardrail/blob/master/LICENSE"))

scalaVersion := "2.12.6"
scalacOptions += "-feature"

// Versioning
enablePlugins(GitBranchPrompt)
enablePlugins(GitVersioning)
git.useGitDescribe := true


// Dependencies
resolvers += Resolver.bintrayRepo("twilio", "releases")
libraryDependencies += "com.twilio" %% "guardrail" % "0.37.0"

// Release
bintrayOrganization := Some("twilio")
bintrayReleaseOnPublish := false
bintrayRepository := {
  if (isSnapshot.value) "snapshots"
  else "releases"
}
publishMavenStyle := true
