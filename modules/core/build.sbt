enablePlugins(SbtPlugin)
enablePlugins(BuildInfoPlugin)

name := "sbt-guardrail-core"
description := "Core of sbt-guardrail plugin, for custom forks of guardrail"

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")

// Dependencies
libraryDependencies += "dev.guardrail" %% "guardrail-core" % s"0.65.4"
libraryDependencies += "dev.guardrail" %% "guardrail" % s"0.65.5"

buildInfoKeys := Seq[BuildInfoKey](organization, version)
buildInfoPackage := "dev.guardrail.sbt"
