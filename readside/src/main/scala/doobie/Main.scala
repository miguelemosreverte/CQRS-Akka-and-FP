import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts
import cats._
import cats.data._
import cats.effect._
import cats.implicits._
import com.typesafe.config.{Config, ConfigFactory}

object Main extends App {

  // We need a ContextShift[IO] before we can construct a Transactor[IO]. The passed ExecutionContext
  // is where nonblocking operations will be executed. For testing here we're using a synchronous EC.
  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  object infrastructure {

    lazy val setup: Config = ConfigFactory.load().getConfig("postgres.setup")

    implicit lazy val tr: Transactor[IO] = {

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

  val xa: doobie.Transactor[IO] = infrastructure.tr

  def insert1(countryCode: String, rank: Int): Update0 =
    sql"insert into country_gdp_ranking (countryCode, rank) values ($countryCode, $rank)".update

/*  insert1("China", 1).run.transact(xa).unsafeRunSync
  insert1("EEUU", 2).run.transact(xa).unsafeRunSync
  insert1("Spain", 5).run.transact(xa).unsafeRunSync
  insert1("Argentina", 24).run.transact(xa).unsafeRunSync
*/
  case class Gdp(countryCode: String, rank: Int)


  def insert2(countryCode: String, year: Int, yearlyPopulationDelta: Long): Update0 =
    sql"insert into country_yearly_population_delta (countryCode, year, yearly_population_delta) values ($countryCode, $year, $yearlyPopulationDelta)".update
  insert2("Argentina", 2008, 1000).run.transact(xa).unsafeRunSync
  insert2("Argentina", 2010, 1000).run.transact(xa).unsafeRunSync
  insert2("Argentina", 2011, 1100).run.transact(xa).unsafeRunSync
  insert2("Argentina", 2012, 1200).run.transact(xa).unsafeRunSync
  insert2("Argentina", 2013, 1300).run.transact(xa).unsafeRunSync
  insert2("Argentina", 2014, 1400).run.transact(xa).unsafeRunSync
  insert2("Argentina", 2015, 2000).run.transact(xa).unsafeRunSync

  insert2("China", 2009, 1000).run.transact(xa).unsafeRunSync
  insert2("China", 2010, 1000).run.transact(xa).unsafeRunSync
  insert2("China", 2011, 1000).run.transact(xa).unsafeRunSync
  insert2("China", 2012, 1000).run.transact(xa).unsafeRunSync
  insert2("China", 2013, 1000).run.transact(xa).unsafeRunSync
  insert2("China", 2014, 1000).run.transact(xa).unsafeRunSync
  insert2("China", 2015, 1000).run.transact(xa).unsafeRunSync

  insert2("EEUU", 2008, 1000).run.transact(xa).unsafeRunSync
  insert2("EEUU", 2010, 1000).run.transact(xa).unsafeRunSync
  insert2("EEUU", 2011, 1100).run.transact(xa).unsafeRunSync
  insert2("EEUU", 2012, 1200).run.transact(xa).unsafeRunSync
  insert2("EEUU", 2013, 1300).run.transact(xa).unsafeRunSync
  insert2("EEUU", 2014, 1400).run.transact(xa).unsafeRunSync
  insert2("EEUU", 2015, 1500).run.transact(xa).unsafeRunSync

  insert2("Spain", 2008, 1000).run.transact(xa).unsafeRunSync
  insert2("Spain", 2010, 1000).run.transact(xa).unsafeRunSync
  insert2("Spain", 2011, 1100).run.transact(xa).unsafeRunSync
  insert2("Spain", 2012, 1200).run.transact(xa).unsafeRunSync
  insert2("Spain", 2013, 1300).run.transact(xa).unsafeRunSync
  insert2("Spain", 2014, 1400).run.transact(xa).unsafeRunSync
  insert2("Spain", 2015, 1500).run.transact(xa).unsafeRunSync
  insert2("Spain", 2020, 1500).run.transact(xa).unsafeRunSync
}
