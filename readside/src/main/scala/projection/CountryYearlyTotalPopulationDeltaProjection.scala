package projection

import cats.effect.IO
import doobie.implicits._
import entities.marshalling._
import entities.{CountryGrossDomesticProductGlobalRanking, CountryYearlyPopulationDelta, CountryYearlyTotalPopulation}
import pub_sub.algebra.KafkaKeyValueLike.KafkaKeyValue
import pub_sub.algebra.MessageProcessor.MessageProcessor
import pub_sub.interpreter.fs2.MessageProcessor.{AlgorithmOutput, MessageProcessorOutput}

object CountryYearlyTotalPopulationDeltaProjection {

  def start(
      readsideMessageProcessor: MessageProcessor[MessageProcessorOutput, AlgorithmOutput]
  )(implicit transactor: doobie.Transactor[IO]) = {

    readsideMessageProcessor("readside")(
      CountryYearlyPopulationDelta.name
    )({ case KafkaKeyValue(key, json) =>
      import serialization.decode

      decode[CountryYearlyPopulationDelta](json) match {
        case Left(value) =>
          Left(value)
        case Right(e @ CountryYearlyPopulationDelta(country, year, populationDelta)) =>
          println(s"iNSERTINT CountryYearlyTotalPopulation ${e}")
          sql"insert into country_yearly_population_delta (countryCode, year, yearly_population_delta) values ($country, $year, $populationDelta)".update.run
            .transact(transactor)
            .unsafeRunSync
          Right(IO.pure((key, json)))
      }
    })
  }

}
