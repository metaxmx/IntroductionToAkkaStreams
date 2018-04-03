
logLevel := Level.Warn

lazy val akkaStreamsExamples = (project in file(".")).settings(
  name := "IntroductionToAkkaStreams",
  version := "1.0",
  scalaVersion := "2.12.5",
  scalacOptions ++= Seq("-encoding", "utf8"),
  javacOptions ++= Seq("-source", "1.8", "-target", "1.8"),
  mainClass in (Compile, run) := Some("com.illucit.examples.WebSocketExample"),
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-stream" % "2.5.11"  withSources() withJavadoc(),
    "com.typesafe.akka" %% "akka-http"   % "10.1.1" withSources() withJavadoc()
  )
)