{
  val pluginVersion = System.getProperty("plugin.version")
  if(pluginVersion == null)
    throw new RuntimeException("""|The system property 'plugin.version' is not defined.
                                  |Specify this property using the scriptedLaunchOpts -D.""".stripMargin)
  else addSbtPlugin("dev.guardrail" % "sbt-guardrail" % pluginVersion)
}

// libraryDependencies += "dev.guardrail" %% "guardrail-java-dropwizard" % "<once 1.0.0 dep changes propagate>"
