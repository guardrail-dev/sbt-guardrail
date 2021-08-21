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

ThisBuild / scalaVersion := "2.12.14"
ThisBuild / scalacOptions ++= List("-feature", "-Xexperimental")

// Versioning
enablePlugins(GitBranchPrompt)
enablePlugins(GitVersioning)
git.useGitDescribe := true

git.gitDescribedVersion := git.gitDescribedVersion(v => {
  import scala.sys.process._
  val nativeGitDescribeResult = ("git describe --tags HEAD" !!).trim
  git.defaultTagByVersionStrategy(nativeGitDescribeResult)
}).value

git.gitUncommittedChanges := git.gitCurrentTags.value.isEmpty

val commonSettings = Seq(
  // Release
  publishMavenStyle := true,
  sonatypeCredentialHost := "s01.oss.sonatype.org",
)


scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + version.value)
}

scriptedDependencies := {
  def use[A](@deprecated("unused", "") x: A*): Unit = () // avoid unused warnings
  val analysis = (Keys.compile in Test).value
  val pubPlug = (publishLocal).value
  val pubCore = (core/publishLocal).value
  use(analysis, pubPlug, pubCore)
}

lazy val root = (project in file("."))
  .settings(commonSettings)
  .dependsOn(core)
  .aggregate(core)

lazy val core = (project in file("modules/core"))
  .settings(commonSettings)
