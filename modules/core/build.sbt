enablePlugins(SbtPlugin)

name := "sbt-guardrail-core"
description := "Core of sbt-guardrail plugin, for custom forks of guardrail"

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")

// Dependencies
resolvers += Resolver.bintrayRepo("twilio", "releases")
libraryDependencies += "com.twilio" %% "guardrail" % "0.58.0"

// Pretty disappointed this has to be copied here
bintrayRepository := {
  if (isSnapshot.value) "snapshots"
  else "releases"
}
