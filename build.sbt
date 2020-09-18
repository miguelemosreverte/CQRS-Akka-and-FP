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
      common
    )


lazy val globalResources = file("resources")

lazy val common = (project in file("./common"))

lazy val pcs = project
  .settings(
    Seq(
      Test / parallelExecution := true,
      unmanagedResourceDirectories in Compile += globalResources
    )
  )
  .settings(commonSettings)
  .settings(modulesSettings)
  .settings(
    name := "pcs",
    assemblySettings
  )
  .dependsOn(
    common % "compile->compile;test->test"
  )
  .enablePlugins(JavaServerAppPackaging, DockerPlugin)
  .settings(
    mainClass := Some("Main")
  )
  .settings(
    dockerBaseImage := "openjdk:8",
    dockerUsername := Some("pcs"),
    dockerEntrypoint := Seq("/opt/docker/bin/pcs"),
    dockerExposedPorts := Seq(
        2551, 2552, 2553, 8081, 8083, 8084, 8558, 9095, 5266
      )
  )
  .settings(
    mainClass in (Compile, run) := Some("Main")
  )
