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

ThisBuild / scalacOptions := Seq("-target:jvm-1.8")
//ThisBuild / pluginCrossBuild / sbtVersion := "1.5.0" //breaks on windows https://github.com/sbt/sbt/issues/7082
ThisBuild / scalacOptions ++= List("-feature", "-Xexperimental")

libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % "3.2.20" % Test,
    "org.scalacheck" %% "scalacheck" % "1.19.0" % Test,
    "org.scalatestplus" %% "scalacheck-1-19" % "3.2.19.0" % Test
  )

// Versioning
enablePlugins(GitBranchPrompt)
enablePlugins(GitVersioning)

git.gitUncommittedChanges := git.gitCurrentTags.value.isEmpty

val commonSettings: SettingsDefinition = Seq(
  // Release
  publishMavenStyle := true,
  evictionErrorLevel := Level.Debug
)


scriptedBufferLog := false

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
