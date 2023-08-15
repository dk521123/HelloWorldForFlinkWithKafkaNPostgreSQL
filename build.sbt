ThisBuild / resolvers ++= Seq(
    "Apache Development Snapshot Repository" at "https://repository.apache.org/content/repositories/snapshots/",
    Resolver.mavenLocal
)

name := "hello-flink-postgre"

version := "0.1-SNAPSHOT"

organization := "dk.com"

// Modify
ThisBuild / scalaVersion := "2.12.18"
// Modify
val flinkVersion = "1.16.1"
val circeVersion = "0.14.1"

val flinkDependencies = Seq(
  // Add from
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "org.slf4j" % "slf4j-api" % "2.0.7",
  "org.apache.logging.log4j" % "log4j-core" % "2.20.0",
  "org.apache.logging.log4j" % "log4j-slf4j2-impl" % "2.20.0",
  "org.apache.flink" % "flink-clients" % flinkVersion,
  "org.apache.flink" % "flink-state-processor-api" % flinkVersion,
  "org.apache.flink" % "flink-connector-kafka" % "1.17.1",
  // Add to
  //"org.apache.flink" %% "flink-clients" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-scala" % flinkVersion % "provided",
  "org.apache.flink" %% "flink-streaming-scala" % flinkVersion % "provided")

lazy val root = (project in file(".")).
  settings(
    libraryDependencies ++= flinkDependencies
  )

assembly / mainClass := Some("dk.com.HelloFlinkKafkaPostgre")

// make run command include the provided dependencies
Compile / run  := Defaults.runTask(Compile / fullClasspath,
                                   Compile / run / mainClass,
                                   Compile / run / runner
                                  ).evaluated

// stays inside the sbt console when we press "ctrl-c" while a Flink programme executes with "run" or "runMain"
Compile / run / fork := true
Global / cancelable := true

// exclude Scala library from assembly
assembly / assemblyOption  := (assembly / assemblyOption).value.copy(includeScala = false)
