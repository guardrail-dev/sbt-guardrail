enablePlugins(SbtPlugin)

name := "sbt-guardrail-core"
description := "Core of sbt-guardrail plugin, for custom forks of guardrail"

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")

// Dependencies
resolvers += Resolver.bintrayRepo("twilio", "releases")
libraryDependencies += "com.twilio" %% "guardrail" % "0.54.5"
