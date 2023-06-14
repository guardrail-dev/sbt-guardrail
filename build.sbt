enablePlugins(SbtPlugin)

name := "sbt-guardrail"
ThisBuild / organization := "dev.guardrail"
description := "Principled code generation from OpenAPI specifications, sbt plugin"
ThisBuild / homepage := Some(url("https://github.com/guardrail-dev/sbt-guardrail"))
ThisBuild / licenses += ("MIT", url("https://github.com/guardrail-dev/guardrail/blob/master/LICENSE"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/guardrail-dev/sbt-guardrail"),
    "scm:git@github.com:guardrail-dev/sbt-guardrail.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "blast_hardcheese",
    name = "Devon Stewart",
    email = "blast@hardchee.se",
    url = url("http://hardchee.se/")
  )
)

ThisBuild / scalaVersion := "2.13.11"
ThisBuild / scalacOptions ++= List("-feature", "-Xexperimental")

libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.2.16" % Test,
    "org.scalacheck" %% "scalacheck" % "1.17.0" % Test,
    "org.scalatestplus" %% "scalatestplus-scalacheck" % "3.1.0.0-RC2" % Test
  )

// Versioning
enablePlugins(GitBranchPrompt)
enablePlugins(GitVersioning)
git.useGitDescribe := true

git.gitDescribedVersion := git.gitDescribedVersion(v => {
  import scala.sys.process._
  val nativeGitDescribeResult = ("git describe --tags --always HEAD" !!).trim
  git.defaultTagByVersionStrategy(nativeGitDescribeResult)
}).value

git.gitUncommittedChanges := git.gitCurrentTags.value.isEmpty

val commonSettings = Seq(
  // Release
  publishMavenStyle := true,
  sonatypeCredentialHost := "s01.oss.sonatype.org",
  evictionErrorLevel := Level.Debug
)


scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
}

scriptedDependencies := {
  Def.sequential(
    (Test / Keys.compile),
    (publishLocal),
    (core/publishLocal)
  ).value
}

lazy val root = (project in file("."))
  .settings(commonSettings)
  .dependsOn(core)
  .aggregate(core)

lazy val core = (project in file("modules/core"))
  .settings(commonSettings)

addCommandAlias("publishLegacy", "set ThisBuild / organization := \"com.twilio\"; set publishTo := sonatypePublishToBundle.value; publishSigned")
