package actor_model

import scala.concurrent.duration._
import scala.util.Try
import akka.actor.ActorSystem
import akka.pattern.ask
import com.typesafe.config.{Config, ConfigFactory}
import actor_model.system_parallelizable.ActorSystemGenerator.RunTest
import actor_model.system_parallelizable.{ActorSystemParallelizerBuilder, AvailablePortProvider}
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.flatspec.AnyFlatSpecLike
import org.scalatest.matchers.should.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, RandomTestOrder}
import serialization.EventSerializer
import utils.Generators

object ActorSpec {

  def system: ActorSystem = {
    val availablePort = AvailablePortProvider.port
    Generators.actorSystem(availablePort)
  }
}

abstract class ActorSpec
    extends AnyFlatSpecLike
    with Matchers
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with Eventually
    with IntegrationPatience
    with RandomTestOrder
    with ScalaFutures {

  def parallelActorSystemRunner(testContext: ActorSystem => Unit): Unit =
    ActorSystemParallelizerBuilder.actor
      .ask(RunTest(testContext))(2.minutes)
      .mapTo[Try[Unit]]
      .futureValue(timeout(2.minutes))
      .get
}
