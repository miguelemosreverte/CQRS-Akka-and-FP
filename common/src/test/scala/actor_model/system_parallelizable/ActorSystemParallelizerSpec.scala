package actor_model.system_parallelizable

import scala.concurrent.duration._
import scala.util.Try

import akka.actor.ActorSystem
import akka.pattern.ask
import com.typesafe.config.{Config, ConfigFactory}
import actor_model.system_parallelizable.ActorSystemGenerator.RunTest
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, RandomTestOrder}
import org.slf4j.{Logger, LoggerFactory}

class ActorSystemParallelizerSpec
    extends AnyFlatSpecLike
    with Matchers
    with ScalaFutures
    with BeforeAndAfterAll
    with Eventually
    with RandomTestOrder {

  val log: Logger = LoggerFactory.getLogger(this.getClass)

  def parallelActorSystemRunner(testContext: ActorSystem => Unit): Unit =
    ActorSystemParallelizerBuilder.actor
      .ask(RunTest(testContext))(2.minutes)
      .mapTo[Try[Unit]]
      .futureValue(timeout(2.minutes))
      .get
}
