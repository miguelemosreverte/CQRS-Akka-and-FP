package country_gdp_ranking.infrastructure.consumers

import akka.Done
import akka.actor.ActorRef
import country_gdp_ranking.application.commands.CountryCommands.AddGDP

import scala.concurrent.Future
import country_gdp_ranking.infrastructure.marshalling._
import akka.entity.AskPattern._
import pub_sub.algebra.KafkaKeyValueLike.KafkaKeyValue
object AddGdpTransaction {
  val topic = "AddGdpTransaction"

  def processMessage(actorRef: ActorRef)(input: KafkaKeyValue): Either[Throwable, Future[Done]] = {
    println("AddGdpTransaction|processMessage")
    serialization
      .decode[AddGDP](input.json)
      .map { serialized =>
        actorRef.ask[akka.Done](serialized)
      }
  }

}
