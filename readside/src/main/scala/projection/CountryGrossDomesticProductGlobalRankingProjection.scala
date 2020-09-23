package projection

import doobie.implicits._
import doobie.Transactor
import entities.marshalling._
import doobie.util.ExecutionContexts
import cats.effect.{ExitCode, IO, IOApp}
import com.typesafe.config.{Config, ConfigFactory}
import pub_sub.algebra.KafkaKeyValueLike.KafkaKeyValue
import pub_sub.algebra.MessageProcessor.MessageProcessor
import org.apache.kafka.common.serialization.StringDeserializer
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, Deserializer}
import pub_sub.interpreter.fs2.MessageBrokerRequirements.productionSettings
import entities.{CountryGrossDomesticProductGlobalRanking, CountryYearlyTotalPopulation}
import pub_sub.interpreter.fs2.MessageProcessor.{fs2MessageProcessor, AlgorithmOutput, MessageProcessorOutput}

object CountryGrossDomesticProductGlobalRankingProjection {

  def start(
      readsideMessageProcessor: MessageProcessor[MessageProcessorOutput, AlgorithmOutput]
  )(implicit transactor: doobie.Transactor[IO]) = {

    readsideMessageProcessor("readside")(
      CountryGrossDomesticProductGlobalRanking.name
    )({ case KafkaKeyValue(key, json) =>
      import serialization.decode

      decode[CountryGrossDomesticProductGlobalRanking](json) match {
        case Left(value) =>
          Left(value)
        case Right(e @ CountryGrossDomesticProductGlobalRanking(country, globalRanking)) =>
          println(s"iNSERTINT CountryGrossDomesticProductGlobalRanking ${e}")
          sql"insert into country_gdp_ranking (countryCode, rank) values ($country, ${globalRanking})".update.run
            .transact(transactor)
            .unsafeRunSync
          Right(IO.pure((key, json)))
      }
    })

  }

}
