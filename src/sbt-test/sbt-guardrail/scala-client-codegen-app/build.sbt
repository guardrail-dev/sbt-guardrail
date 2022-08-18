name := "sbt-guardrail-scala-client-test-app"

version := "1.0." + System.currentTimeMillis

scalaVersion := "2.12.16"

scalacOptions += "-Xexperimental"

Compile / guardrailTasks := GuardrailHelpers.createGuardrailTasks((Compile / sourceDirectory).value / "openapi") { openApiFile =>
  List(
    ScalaClient(openApiFile.file, pkg = openApiFile.pkg + ".client", imports = List("_root_.support.PositiveLong")),
    ScalaServer(openApiFile.file, pkg = openApiFile.pkg + ".server", imports = List("_root_.support.PositiveLong"))
  )
}

Test / guardrailTasks += ScalaClient.defaults(imports = List(
    "_root_.support.PositiveLong",
    "_root_.support.tests.PositiveLongInstances._"
  ))
Test / guardrailTasks ++= (Test / guardrailDiscoveredOpenApiFiles).value.flatMap { openApiFile =>
  List(
    ScalaClient(openApiFile.file, pkg = openApiFile.pkg + ".client"),
    ScalaServer(openApiFile.file, pkg = openApiFile.pkg + ".server")
  )
}


val circeVersion = "0.13.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"   % "10.2.6",
  "com.typesafe.akka" %% "akka-stream" % "2.6.17",
  "javax.xml.bind"     % "jaxb-api"    % "2.3.1",
  "org.scalatest"     %% "scalatest"   % "3.0.8" % "test"
)
