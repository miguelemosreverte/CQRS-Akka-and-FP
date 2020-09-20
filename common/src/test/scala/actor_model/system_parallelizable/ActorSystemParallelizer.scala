package actor_model.system_parallelizable

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.Try

import akka.actor.{Actor, ActorSystem, Props, Stash}
import akka.pattern.pipe
import com.typesafe.config.Config

class ActorSystemParallelizer(actorSystemNumbers: Seq[Int]) extends Actor with Stash {
  import ActorSystemParallelizer._
  import context.dispatcher
  implicit private val _system: ActorSystem = context.system

  // scalastyle:off
  private var state: Option[ActorSystemParallelizerState] = None
  private var freeActorSystemSeries = List.empty[Int]
  // scalastyle:on

  override def preStart(): Unit =
    _system.scheduler.scheduleOnce(30.seconds, self, CanShutdown)

  override def postStop(): Unit =
    state.foreach(_.freeActorSystems.foreach(_._2.terminate()))

  override def receive: Receive = {
    case cmd: FreeActorSystem =>
      val infrastructureContextNumber = cmd.actorSystem._1
      state = state.map(_.withFreeActorSystem(cmd.actorSystem))
      freeActorSystemSeries = infrastructureContextNumber :: freeActorSystemSeries
      unstashAll()

    case CanShutdown =>
      if (actorSystemNumbers.forall(freeActorSystemSeries.contains)) {
        context.stop(self)
      } else {
        _system.scheduler.scheduleOnce(10.seconds, self, CanShutdown)
      }

    case RunTest(config, test) =>
      state
        .orElse(Some(ActorSystemParallelizerState.fromActorSystemNumbers(config, actorSystemNumbers)))
        .foreach(processTest(test))
  }

  private def processTest(
      test: ActorSystem => Unit
  )(_state: ActorSystemParallelizerState): Unit = _state.nextFreeActorSystem match {
    case None =>
      stash()

    case Some(actorSystem) =>
      val (actorSystemNumber, actorSystemDef) = actorSystem
      state = Some(_state.withBusyActorSystem(actorSystem))
      freeActorSystemSeries = freeActorSystemSeries.filter(_ != actorSystemNumber)

      val testResult = for {
        result <- Future(Try(test(actorSystemDef)))
      } yield result

      testResult.onComplete { _ =>
        self ! FreeActorSystem(actorSystem)
      }
      testResult.pipeTo(sender())
  }
}

object ActorSystemParallelizer {
  def props(actorSystemNumbers: Seq[Int]): Props = Props(new ActorSystemParallelizer(actorSystemNumbers))

  case class FreeActorSystem(actorSystem: (Int, ActorSystem))
  case object CanShutdown
  case class RunTest(config: Config, test: ActorSystem => Unit)
}
