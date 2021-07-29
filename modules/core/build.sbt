enablePlugins(SbtPlugin)

name := "sbt-guardrail-core"
description := "Core of sbt-guardrail plugin, for custom forks of guardrail"

addCompilerPlugin("org.typelevel" %% "kind-projector" % "0.10.3")

val guardrailVersion = "0.64.5"

// Dependencies
libraryDependencies += "com.twilio" %% "guardrail" % guardrailVersion

Compile / sourceGenerators += Def.task {
  val file = (Compile / sourceManaged).value / "com" / "twilio" / "guardrail" / "Versions.scala"
  val content =
    s"""package com.twilio.guardrail
       |private [guardrail] object Versions {
       |  val guardrailVersion = "$guardrailVersion"
       |}
       |""".stripMargin
  IO.write(file, content)
  Seq(file)
}.taskValue
