import doobie._
import doobie.implicits._
import cats._
import cats.effect._
import cats.implicits._
import doobielib.DoobieUtils.PersonTable._

import scala.concurrent.ExecutionContext

object Postgres extends App {
  println("Hello Doobie")

  val program1 = 42.pure[ConnectionIO]
  // program1: ConnectionIO[Int] = Pure(42)

  println(program1)

  import doobie.hikari._

  val dbExecutionContext = ExecutionContext.global // replace with your DB specific EC.
  implicit val contextShift: ContextShift[IO] = IO.contextShift(dbExecutionContext)

  // Resource yielding a transactor configured with a bounded connect EC and an unbounded
  // transaction EC. Everything will be closed and shut down cleanly after use.
  val transactor: Resource[IO, HikariTransactor[IO]] =
    for {
      ce <- ExecutionContexts.fixedThreadPool[IO](32) // our connect EC
      be <- Blocker[IO] // our blocking EC
      xa <- HikariTransactor.newHikariTransactor[IO](
        "org.h2.Driver", // driver classname
        "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", // connect URL
        "sa", // username
        "", // password
        ce, // await connection here
        be // execute JDBC operations here
      )
    } yield {
      println(xa)

      def insert1(name: String, age: Option[Short]): Update0 =
        sql"insert into person (name, age) values ($name, $age)".update
      val insertedRows2 = transactorBlock(insert1("John", Option(35)).run).unsafeRunSync()

      val rows = for {
        row1 <- insert1("Alice", Option(12)).run
        row2 <- insert1("Bob", None).run
        row3 <- insert1("Juan", Option(17)).run
      } yield row1 + row2 + row3

      val insertedRows3 = transactorBlock(rows).unsafeRunSync()

      val JuanAge =
        transactorBlock(sql"select age from person where name = 'Juan'".query[Int].unique)
          .unsafeRunSync()

      println(s""""
      
      Juan age is: ${JuanAge}
      """)
      xa
    }

  val value = transactor.use(42.pure[ConnectionIO].transact[IO]).unsafeRunSync()

  println(transactor)
  println(value)

  val largerProgram = for {
    a <- sql"select 42".query[Int].unique
    b <- sql"select power(5, 2)".query[Int].unique
  } yield {
    println("largerProgram")
    println(a)
    println(b)
    (a, b)
  }
  transactor.use(largerProgram.transact[IO]).unsafeRunSync()

}
