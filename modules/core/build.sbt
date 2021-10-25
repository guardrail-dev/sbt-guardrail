enablePlugins(SbtPlugin)
enablePlugins(BuildInfoPlugin)

name := "sbt-guardrail-core"
description := "Core of sbt-guardrail plugin, for custom forks of guardrail"

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")

// Dependencies
libraryDependencies ++= Seq(
  "dev.guardrail" %% "guardrail" % "0.66.0",
  "dev.guardrail" %% "guardrail-cli" % "0.66.0",
  "dev.guardrail" %% "guardrail-core" % "0.66.0",
  "dev.guardrail" %% "guardrail-java-async-http" % "0.66.0",
  "dev.guardrail" %% "guardrail-java-dropwizard" % "0.66.0",
  "dev.guardrail" %% "guardrail-java-spring-mvc" % "0.66.0",
  "dev.guardrail" %% "guardrail-java-support" % "0.66.0",
  "dev.guardrail" %% "guardrail-scala-akka-http" % "0.67.0",
  "dev.guardrail" %% "guardrail-scala-dropwizard" % "0.66.0",
  "dev.guardrail" %% "guardrail-scala-endpoints" % "0.66.0",
  "dev.guardrail" %% "guardrail-scala-http4s" % "0.66.0",
  "dev.guardrail" %% "guardrail-scala-support" % "0.66.0"
)

buildInfoKeys := Seq[BuildInfoKey](organization, version)
buildInfoPackage := "dev.guardrail.sbt"
