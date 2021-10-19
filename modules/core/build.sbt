enablePlugins(SbtPlugin)
enablePlugins(BuildInfoPlugin)

name := "sbt-guardrail-core"
description := "Core of sbt-guardrail plugin, for custom forks of guardrail"

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")

// Dependencies
libraryDependencies ++= Seq(
  "dev.guardrail" %% "guardrail" % "0.65.5",
  "dev.guardrail" %% "guardrail-cli" % "0.65.2",
  "dev.guardrail" %% "guardrail-core" % "0.65.4",
  "dev.guardrail" %% "guardrail-java-async-http" % "0.65.2",
  "dev.guardrail" %% "guardrail-java-dropwizard" % "0.65.2",
  "dev.guardrail" %% "guardrail-java-spring-mvc" % "0.65.2",
  "dev.guardrail" %% "guardrail-java-support" % "0.65.2",
  "dev.guardrail" %% "guardrail-scala-akka-http" % "0.65.2",
  "dev.guardrail" %% "guardrail-scala-dropwizard" % "0.65.2",
  "dev.guardrail" %% "guardrail-scala-endpoints" % "0.65.2",
  "dev.guardrail" %% "guardrail-scala-http4s" % "0.65.4",
  "dev.guardrail" %% "guardrail-scala-support" % "0.65.2"
)

buildInfoKeys := Seq[BuildInfoKey](organization, version)
buildInfoPackage := "dev.guardrail.sbt"
