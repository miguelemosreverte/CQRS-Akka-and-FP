package actor_model.system_parallelizable

import akka.actor.{Actor, ActorSystem}
import akka.pattern.pipe
import actor_model.ActorSpec
import scala.collection.mutable
import scala.concurrent.Future
import scala.util.Try

class ActorSystemGenerator extends Actor {

  import ActorSystemGenerator._
  import context.dispatcher

  implicit private val _system: ActorSystem = context.system

  override def postStop(): Unit =
    childSystems.foreach(_.terminate())

  val childSystems: mutable.ListBuffer[ActorSystem] = mutable.ListBuffer.empty[ActorSystem]

  override def receive: Receive = {

    case RunTest(test) =>
      val system =
        ActorSpec.system
      childSystems.addOne(system)

      processTest(test, system)

  }

  private def processTest(
      test: ActorSystem => Unit,
      actorSystem: ActorSystem
  ): Unit = {

    val testResult = for {
      result <- Future(Try(test(actorSystem)))
    } yield result

    testResult.pipeTo(sender())
  }

}

object ActorSystemGenerator {
  case class RunTest(test: ActorSystem => Unit)
}
