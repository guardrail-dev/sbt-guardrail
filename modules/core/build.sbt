enablePlugins(SbtPlugin)
enablePlugins(BuildInfoPlugin)

name := "sbt-guardrail-core"
description := "Core of sbt-guardrail plugin, for custom forks of guardrail"

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")

// Dependencies
libraryDependencies += "dev.guardrail" %% "guardrail" % "0.65.4"
libraryDependencies += "dev.guardrail" %% "guardrail-cli" % "0.65.2" // Temporary, until guardrailRunner gets moved into core

buildInfoKeys := Seq[BuildInfoKey](organization, version)
buildInfoPackage := "dev.guardrail.sbt"
