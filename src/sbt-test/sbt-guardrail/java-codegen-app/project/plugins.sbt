{
  val pluginVersion = System.getProperty("plugin.version")
  if(pluginVersion == null)
    throw new RuntimeException("""|The system property 'plugin.version' is not defined.
                                  |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
  else addSbtPlugin("dev.guardrail" % "sbt-guardrail" % pluginVersion)
}

libraryDependencies += "dev.guardrail" %% "guardrail-java-support" % "0.73.1"
libraryDependencies += "dev.guardrail" %% "guardrail-java-async-http" % "0.72.0"
libraryDependencies += "dev.guardrail" %% "guardrail-java-dropwizard" % "0.72.0"
