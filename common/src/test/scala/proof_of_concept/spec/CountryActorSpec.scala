package proof_of_concept.spec

import akka.Done
import akka.actor.ActorSystem
import org.scalatest.flatspec.AnyFlatSpec
import proof_of_concept.implementation.application.commands.CountryCommands
import proof_of_concept.implementation.domain.GDP
import proof_of_concept.implementation.infrastructure.CountryActor
import akka.entity.AskPattern._
import com.typesafe.config.ConfigFactory
import org.scalatest.matchers.must.Matchers.{be, convertToAnyMustWrapper}
import proof_of_concept.implementation.application.queries.CountryQueries

class CountryActorSpec extends AnyFlatSpec {
  implicit val system = utils.Generators.actorSystem("Test")
  import system.dispatcher

  val actor = CountryActor.start
  for {
    done <- actor.ask[akka.Done](CountryCommands.AddGDP(country = "Argentina", GDP = GDP(10)))
    response <- actor.ask[GDP](CountryQueries.GetCountryStateGDP(country = "Argentina"))
  } yield response must be(GDP(10))
}
