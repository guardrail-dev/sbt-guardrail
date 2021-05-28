name := "sbt-guardrail-scala-client-test-app"

version := "1.0." + System.currentTimeMillis

scalaVersion := "2.12.14"

scalacOptions += "-Xexperimental"

guardrailTasks in Compile := List(
  ScalaClient(file("petstore.json"), pkg="com.example.clients.petstore", imports=List("_root_.support.PositiveLong")),
  ScalaServer(file("petstore.json"), pkg="com.example.servers.petstore", imports=List("_root_.support.PositiveLong"))
)

guardrailTasks in Test := List(
  ScalaClient(file("petstore.json"), pkg="com.example.tests.clients.petstore", imports=List("_root_.support.PositiveLong", "_root_.support.tests.PositiveLongInstances._")),
  ScalaServer(file("petstore.json"), pkg="com.example.tests.servers.petstore", imports=List("_root_.support.PositiveLong", "_root_.support.tests.PositiveLongInstances._"))
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
