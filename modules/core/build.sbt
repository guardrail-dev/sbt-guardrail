enablePlugins(SbtPlugin)
enablePlugins(BuildInfoPlugin)

name := "sbt-guardrail-core"
description := "Core of sbt-guardrail plugin, for custom forks of guardrail"

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")

// Dependencies
libraryDependencies += "dev.guardrail" %% "guardrail-core" % "0.65.3-SNAPSHOT"
libraryDependencies += "dev.guardrail" %% "guardrail" % "0.65.4-SNAPSHOT"

buildInfoKeys := Seq[BuildInfoKey](organization, version)
buildInfoPackage := "dev.guardrail.sbt"
