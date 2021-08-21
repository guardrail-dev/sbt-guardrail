enablePlugins(SbtPlugin)
enablePlugins(BuildInfoPlugin)

name := "sbt-guardrail-core"
description := "Core of sbt-guardrail plugin, for custom forks of guardrail"

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")

// Dependencies
libraryDependencies += "dev.guardrail" %% "guardrail" % "0.64.4"

buildInfoKeys := Seq[BuildInfoKey](organization)
buildInfoPackage := "dev.guardrail.sbt"
