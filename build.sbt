import Settings._
import sbt.Keys.scalaVersion

lazy val commonSettings = Seq(
  organization in ThisBuild := "example",
  version := "1.0",
  scalaVersion := Dependencies.scalaVersion
)

lazy val global = project
  .in(file("."))
  .settings(
    name := "Example"
  )
  .settings(commonSettings)
  .settings(modulesSettings)
  .settings(mainSettings)
  .settings(testSettings)
  .settings(scalaFmtSettings)
  .settings(testCoverageSettings)
  .settings(CommandAliases.aliases)
  .enablePlugins(ScoverageSbtPlugin)
  .enablePlugins(JavaServerAppPackaging, DockerPlugin)
  .aggregate(
    common,
    it,
    writeside,
    readside
  )

lazy val globalResources = file("resources")

lazy val common = project
lazy val it = project
lazy val writeside = project
lazy val readside = project
