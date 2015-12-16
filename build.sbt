name := "intro-to-akka-streams"

version := "1.0.0"

scalaVersion := "2.11.7"

resolvers += Resolver.bintrayRepo("mfglabs", "maven")

libraryDependencies ++= {
 val akkaVersion    = "2.3.12"
 val streamsVersion = "1.0"
 Seq(
  "com.typesafe.akka"  %%  "akka-actor"                       % akkaVersion,
  "com.typesafe.akka"  %%  "akka-kernel"                      % akkaVersion,
  "com.typesafe.akka"  %%  "akka-stream-experimental"         % streamsVersion,
  "com.typesafe.akka"  %%  "akka-http-core-experimental"      % streamsVersion,
  "com.typesafe.akka"  %% "akka-http-spray-json-experimental" % streamsVersion,
  "com.typesafe.slick" %%  "slick"                            % "3.0.0",
  "com.zaxxer"          %  "HikariCP-java6"                   % "2.3.5",
  "org.postgresql"      %  "postgresql"                       % "9.4-1201-jdbc41",
  "io.scalac"          %%  "reactive-rabbit"                  % "1.0.0",
  "com.typesafe.akka"  %%  "akka-stream-testkit-experimental" % streamsVersion % Test,
  "org.scalatest"      %%  "scalatest"                        % "2.2.4"        % Test
 )
}

fork in Test := true

parallelExecution in Test := false

licenses +=("Apache-2.0", url("http://opensource.org/licenses/apache2.0.php"))

// enable scala code formatting //
import scalariform.formatter.preferences._

scalariformSettings

ScalariformKeys.preferences := ScalariformKeys.preferences.value
  .setPreference(AlignSingleLineCaseStatements, true)
  .setPreference(AlignSingleLineCaseStatements.MaxArrowIndent, 100)
  .setPreference(DoubleIndentClassDeclaration, true)
  .setPreference(RewriteArrowSymbols, true)

// enable updating file headers //
import de.heikoseeberger.sbtheader.license.Apache2_0

headers := Map(
 "scala" -> Apache2_0("2015", "Dennis Vriend"),
 "conf" -> Apache2_0("2015", "Dennis Vriend", "#")
)

enablePlugins(AutomateHeaderPlugin)