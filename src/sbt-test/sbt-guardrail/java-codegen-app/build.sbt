name := "sbt-guardrail-java-test-app"

version := "1.0." + System.currentTimeMillis

scalaVersion := "2.13.6"

scalacOptions += "-Xexperimental"

guardrailTasks in Compile := List(
  JavaClient(file("petstore.json"), pkg="com.example.clients.petstore")
)

// workaround for SBT issue.  see: https://github.com/sbt/sbt/issues/1664
managedSourceDirectories in Compile += (sourceManaged in Compile).value

val jacksonVersion = "2.10.1"
val javaxAnnotationVersion = "1.3.2"

libraryDependencies ++= Seq(
  "com.fasterxml.jackson.core"        % "jackson-databind"          % jacksonVersion,
  "com.fasterxml.jackson.datatype"    % "jackson-datatype-jdk8"     % jacksonVersion,
  "com.fasterxml.jackson.datatype"    % "jackson-datatype-jsr310"   % jacksonVersion,
  "org.asynchttpclient"               % "async-http-client"         % "2.10.4",
  "javax.annotation"                  %  "javax.annotation-api"     % javaxAnnotationVersion, // for jdk11
  "javax.xml.bind"                    % "jaxb-api"                  % "2.3.1",
  "org.scalatest"                    %% "scalatest"                 % "3.0.8" % "test"
)
