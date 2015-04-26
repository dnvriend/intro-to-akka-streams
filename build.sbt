name := "intro-to-akka-streams"

version := "1.0.0"

scalaVersion := "2.11.6"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-slf4j" % "2.3.10"% Test withSources() withJavadoc(),
  "com.typesafe.akka" %% "akka-stream-experimental" % "1.0-RC1" % Test withSources() withJavadoc(),
  "org.scalatest" %% "scalatest" % "2.2.4" % Test withSources() withJavadoc()
)
