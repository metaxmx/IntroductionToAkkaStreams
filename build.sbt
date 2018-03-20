
lazy val akkaStreamsExamples = (project in file(".")).settings(
  name := "IntroductionToAkkaStreams",
  version := "1.0",
  scalaVersion := "2.12.4",
  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-stream" % "2.5.5"  withSources() withJavadoc(),
    "com.typesafe.akka" %% "akka-http"   % "10.1.0" withSources() withJavadoc()
  )
)