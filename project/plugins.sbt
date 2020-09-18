// Test Coverage
addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.6.0") // https://github.com/scoverage/sbt-scoverage

// Scala style
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.3.0")

// Native Packager
addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.5.1")
// Revolver
addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

//addSbtPlugin("io.kamon" % "sbt-kanela-runner" % "2.0.3")

addSbtPlugin("io.get-coursier" % "sbt-coursier" % "2.0.0-RC2")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.3.2")

addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.5.7")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.5")
