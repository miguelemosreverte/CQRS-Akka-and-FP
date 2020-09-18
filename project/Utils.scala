import scala.Console._

object Utils {

  def cyan(projectName: String): String = CYAN + projectName + RESET

  def fancyPrompt(projectName: String): String =
    s"""|
      |[info] Welcome to the ${cyan(projectName)} project!
        |sbt> """.stripMargin

}
