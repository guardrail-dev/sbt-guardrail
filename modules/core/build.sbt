enablePlugins(SbtPlugin)
enablePlugins(BuildInfoPlugin)

name := "sbt-guardrail-core"
description := "Core of sbt-guardrail plugin, for custom forks of guardrail"

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")

// Dependencies
libraryDependencies ++= Seq(
  "dev.guardrail" %% "guardrail" % "0.68.1",
  "dev.guardrail" %% "guardrail-core" % "0.68.1",
  "dev.guardrail" %% "guardrail-java-support" % "0.67.2",
  "dev.guardrail" %% "guardrail-scala-support" % "0.68.1",

  // Pending removal, before we hit 1.0.0, as per https://github.com/guardrail-dev/guardrail/issues/1195
  "dev.guardrail" %% "guardrail-java-async-http" % "0.67.1",
  "dev.guardrail" %% "guardrail-java-dropwizard" % "0.67.1",
  "dev.guardrail" %% "guardrail-java-spring-mvc" % "0.67.1",
  "dev.guardrail" %% "guardrail-scala-akka-http" % "0.69.0",
  "dev.guardrail" %% "guardrail-scala-dropwizard" % "0.67.1",
  "dev.guardrail" %% "guardrail-scala-endpoints" % "0.67.1",
  "dev.guardrail" %% "guardrail-scala-http4s" % "0.68.0"
)

buildInfoKeys := Seq[BuildInfoKey](organization, version)
buildInfoPackage := "dev.guardrail.sbt"
