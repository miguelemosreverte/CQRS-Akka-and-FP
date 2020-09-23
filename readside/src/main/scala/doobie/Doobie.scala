package doobie

import cats.effect.{ContextShift, IO}
import com.typesafe.config.{Config, ConfigFactory}

object Doobie {
  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

  implicit def getTransactor: doobie.Transactor[IO] = {
    lazy val setup: Config = ConfigFactory.load().getConfig("postgres.setup")

    val host = setup.getString("host")

    val port = setup.getString("port")

    val user = setup.getString("user")

    val pass = setup.getString("pass")

    Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",
      s"jdbc:postgresql://$host:$port/postgres",
      user,
      pass
    )

  }
}
