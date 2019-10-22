sbtPlugin := true

name := "sbt-guardrail"
organization := "com.twilio"
description := "Principled code generation for Scala services from OpenAPI specifications, sbt plugin"
homepage := Some(url("https://github.com/twilio/sbt-guardrail"))
licenses += ("MIT", url("https://github.com/twilio/guardrail/blob/master/LICENSE"))
bintrayPackageLabels := Seq(
  "codegen",
  "openapi",
  "swagger",
  "sbt"
)

scmInfo := Some(
  ScmInfo(
    url("https://github.com/twilio/sbt-guardrail"),
    "scm:git@github.com:twilio/sbt-guardrail.git"
  )
)

developers := List(
  Developer(
    id = "blast_hardcheese",
    name = "Devon Stewart",
    email = "blast@hardchee.se",
    url = url("http://hardchee.se/")
  )
)

scalaVersion := "2.12.10"
scalacOptions ++= List("-feature", "-Xexperimental")

addCompilerPlugin("org.spire-math" %% "kind-projector" % "0.9.9")

// Versioning
enablePlugins(GitBranchPrompt)
enablePlugins(GitVersioning)
git.useGitDescribe := true


// Dependencies
resolvers += Resolver.bintrayRepo("twilio", "releases")
libraryDependencies += "com.twilio" %% "guardrail" % "0.54.1"

// Release
bintrayOrganization := Some("twilio")
bintrayReleaseOnPublish := false
bintrayRepository := {
  if (isSnapshot.value) "snapshots"
  else "releases"
}

publishMavenStyle := true

addCommandAlias(
  "publishBintray",
  "; set publishTo := (publishTo in bintray).value; publishSigned"
)
addCommandAlias(
  "publishSonatype",
  "; set publishTo := sonatypePublishTo.value; publishSigned"
)
