package actor_model.system_parallelizable

import akka.actor.{ActorRef, ActorSystem, Props}
import actor_model.ActorSpec
import utils.Generators

object ActorSystemParallelizerBuilder {

  private val availablePort: Int = AvailablePortProvider.port
  private val system = Generators.actorSystem(availablePort, "ActorSystemParallelizerBuilder")
  lazy val actor: ActorRef =
    system.actorOf(Props(new ActorSystemGenerator), s"acceptance-test-actor")
}
