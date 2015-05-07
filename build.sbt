name := "intro-to-akka-streams"

version := "1.0.0"

scalaVersion := "2.11.6"

libraryDependencies ++= { 
 val streamsVersion = "1.0-RC2"
 Seq(
  "com.typesafe.akka" %% "akka-slf4j"                       % "2.3.10",
  "com.typesafe.akka" %% "akka-stream-experimental"         % streamsVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit-experimental" % streamsVersion % Test,
  "org.scalatest"     %% "scalatest"                        % "2.2.4"        % Test
 )
}
