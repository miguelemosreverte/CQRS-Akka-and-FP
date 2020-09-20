package proof_of_concept.implementation.infrastructure.consumers

import akka.Done
import akka.actor.ActorRef
import proof_of_concept.implementation.application.commands.CountryCommands.AddGDP
import scala.concurrent.Future
import proof_of_concept.implementation.infrastructure.marshalling._
import akka.entity.AskPattern._

object AddGdpTransaction {
  val topic = "AddGdpTransaction"

  def processMessage(actorRef: ActorRef)(input: String): Either[Throwable, Future[Done]] = {
    println("AddGdpTransaction|processMessage")
    serialization
      .decode[AddGDP](input)
      .map { serialized =>
        actorRef.ask[akka.Done](serialized)
      }
  }

}
