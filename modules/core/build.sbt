enablePlugins(SbtPlugin)
enablePlugins(BuildInfoPlugin)

name := "sbt-guardrail-core"
description := "Core of sbt-guardrail plugin, for custom forks of guardrail"

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")

// Explicitly pinning this to a binding so we can grep for it
val guardrailCoreVersion = "1.0.0-M1"
resolvers +=
  "Sonatype OSS Snapshots" at "https://s01.oss.sonatype.org/content/repositories/snapshots"

// Dependencies
libraryDependencies ++= Seq(
  "dev.guardrail" %% "guardrail-core" % guardrailCoreVersion,
  "org.snakeyaml" % "snakeyaml-engine" % "2.7"
)

buildInfoKeys := Seq[BuildInfoKey](organization, version)
buildInfoPackage := "dev.guardrail.sbt"
