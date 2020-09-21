import Settings._

lazy val common = ProjectRef(file("./"), "common")

lazy val writeside = project
  .in(file("."))
  .settings(
    name := "writeside"
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
  .dependsOn(
    common % "compile->compile;test->test"
  )
