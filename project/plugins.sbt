// to format scala source code
addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.6.0")

// enable updating file headers eg. for copyright
addSbtPlugin("de.heikoseeberger" % "sbt-header" % "1.8.0")

// generates Scala source from your build definitions //
// see: https://github.com/sbt/sbt-buildinfo
addSbtPlugin("com.eed3si9n" % "sbt-buildinfo" % "0.5.0")

// enable playframework
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.14")
