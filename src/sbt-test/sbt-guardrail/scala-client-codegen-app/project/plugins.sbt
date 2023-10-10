{
  val pluginVersion = System.getProperty("plugin.version")
  if(pluginVersion == null)
    throw new RuntimeException("""|The system property 'plugin.version' is not defined.
                                  |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
  else addSbtPlugin("dev.guardrail" % "sbt-guardrail" % pluginVersion)
}

libraryDependencies += "dev.guardrail" %% "guardrail-scala-support" % "0.75.3"
libraryDependencies += "dev.guardrail" %% "guardrail-scala-akka-http" % "0.76.0"
