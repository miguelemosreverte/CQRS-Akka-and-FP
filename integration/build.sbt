import Settings._

lazy val common = ProjectRef(file("./"), "common")
lazy val domain = ProjectRef(file("./"), "domain")
lazy val writeside = ProjectRef(file("./"), "writeside")
lazy val readside = ProjectRef(file("./"), "readside")

lazy val integration = project
  .in(file("."))
  .settings(
    name := "integration"
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
    common % "compile->compile;test->test",
    domain % "compile->compile;test->test",
    writeside % "compile->compile;test->test",
    readside % "compile->compile;test->test"
  )
