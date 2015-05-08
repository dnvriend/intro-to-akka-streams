name := "intro-to-akka-streams"

version := "1.0.0"

scalaVersion := "2.11.6"

libraryDependencies ++= {
 val akkaVersion    = "2.3.10"
 val streamsVersion = "1.0-RC2"
 Seq(
  "com.typesafe.akka"  %%  "akka-actor"                      % akkaVersion,
  "com.typesafe.akka"  %%  "akka-kernel"                     % akkaVersion,
  "com.typesafe.akka"  %%  "akka-stream-experimental"        % streamsVersion,
  "com.typesafe.slick" %% "slick"                            % "3.0.0",
  "com.zaxxer"          % "HikariCP-java6"                   % "2.3.5",
  "org.postgresql"      % "postgresql"                       % "9.4-1201-jdbc41",
  "io.scalac"          %% "reactive-rabbit"                  % "1.0.0",
  "com.typesafe.akka"  %% "akka-slf4j"                       % "2.3.10",
  "com.typesafe.akka"  %% "akka-stream-testkit-experimental" % streamsVersion % Test,
  "org.scalatest"      %% "scalatest"                        % "2.2.4"        % Test
 )
}
