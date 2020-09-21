import Settings._

lazy val common = project
  .in(file("."))
  .settings(
    name := "common"
  )
  .settings(modulesSettings)
  .settings(mainSettings)
  .settings(testSettings)
  .settings(scalaFmtSettings)
  .settings(testSettings)
  .settings(testCoverageSettings)
  .settings(CommandAliases.aliases)
  .enablePlugins(ScoverageSbtPlugin)
  .enablePlugins(JavaServerAppPackaging, DockerPlugin)
