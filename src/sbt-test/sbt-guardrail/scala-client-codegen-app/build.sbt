name := "sbt-guardrail-scala-client-test-app"

version := "1.0." + System.currentTimeMillis

scalaVersion := "2.12.10"

scalacOptions += "-Xexperimental"

guardrailTasks in Compile := List(
  ScalaClient(file("petstore.json"), pkg="com.example.clients.petstore", imports=List("support.PositiveLong")),
  ScalaServer(file("petstore.json"), pkg="com.example.servers.petstore", imports=List("support.PositiveLong"))
)

val circeVersion = "0.13.0"

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser"
).map(_ % circeVersion)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http"   % "10.1.10",
  "com.typesafe.akka" %% "akka-stream" % "2.5.23",
  "javax.xml.bind"     % "jaxb-api"    % "2.3.1",
  "org.scalatest"     %% "scalatest"   % "3.0.8" % "test"
)
