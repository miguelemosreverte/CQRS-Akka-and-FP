package spec

import java.util.concurrent.Executors

import actor_model.ActorSpec
import akka.actor.ActorSystem
import akka.cluster.Cluster
import cats.effect.internals.IOAppPlatform
import cats.effect.{ContextShift, ExitCode, IO, IOApp, Timer}
import country_yearly_population_delta.application.events.CountryEvents.AddedYearlyCountryPopulationGrowth
import country_yearly_population_delta.infrastructure.consumers.AddYearlyPopulationGrowthTransaction
import doobie.Doobie.getTransactor
import doobie.implicits._
import fs2.kafka.{AutoOffsetReset, ConsumerSettings, Deserializer}
import org.apache.kafka.common.serialization.StringDeserializer
import pub_sub.algebra.MessageProcessor.MessageProcessor
import pub_sub.algebra.MessageProducer
import pub_sub.algebra.MessageProducer.MessageProducer
import pub_sub.interpreter.alpakka.MessageBrokerRequirements
import pub_sub.interpreter.fs2.MessageBrokerRequirements
import pub_sub.interpreter.fs2.MessageBrokerRequirements.productionSettings
import pub_sub.interpreter.fs2.MessageProcessor.{fs2MessageProcessor, AlgorithmOutput, MessageProcessorOutput}
import spec.HighestGrowingCountriesRankedByGDPSpec.TestContext
import pub_sub.scaladsl.MessageProducerDSL._
import use_cases.highest_growing_countries_ranked_by_gdp.stage_1.GdpGet.run
import use_cases.highest_growing_countries_ranked_by_gdp.stage_1.PopulationGet.run
import use_cases.highest_growing_countries_ranked_by_gdp.stage_1.{GdpGet, PopulationGet}

import scala.concurrent.ExecutionContext
object HighestGrowingCountriesRankedByGDPSpec {

  case class TestContext(
      messageProcessor: MessageProcessor[_, _],
      messageProducer: MessageProducer[_]
  )

}

abstract class HighestGrowingCountriesRankedByGDPSpec[AlgorithmOut, Out](
    getContext: (ActorSystem => TestContext)
) extends ActorSpec {

  import cats.effect.{ContextShift, IO}
  implicit val ioContextShift: ContextShift[IO] =
    IO.contextShift(ExecutionContext.fromExecutor(ExecutionContext.global))
  implicit val timer = IO.timer(ExecutionContext.fromExecutor(Executors.newFixedThreadPool(10)))

  "stage 1" should "work as expected" in {
    // STAGE 1 - PRODUCER
    GdpGet.run(List.empty).unsafeRunSync().code should be(0)
  }

  PopulationGet.run(List.empty).unsafeRunSync().code should be(0)

  // STAGE 3 - READSIDE -- I had to start it on background as I did not had killswitches for FS2
  val readsideMessageProcessor: MessageProcessor[MessageProcessorOutput, AlgorithmOutput] =
    fs2MessageProcessor(
      productionSettings.copy(
        consumerSettings = ConsumerSettings(
          keyDeserializer = Deserializer.delegate[IO, String](new StringDeserializer),
          valueDeserializer = Deserializer.delegate[IO, String](new StringDeserializer)
        ).withAutoOffsetReset(AutoOffsetReset.Earliest)
          .withBootstrapServers("0.0.0.0:9092")
          .withGroupId("default")
      )
    )
  projection.CountryGrossDomesticProductGlobalRankingProjection
    .start(readsideMessageProcessor)
    .unsafeRunSync

  projection.CountryYearlyTotalPopulationDeltaProjection
    .start(readsideMessageProcessor)
    .unsafeRunSync

  "full integration" should "work as expected" in parallelActorSystemRunner { implicit s: ActorSystem =>
    val context = getContext(s)
    val cluster: Cluster = Cluster(s)
    cluster.registerOnMemberUp { () =>
      (0 to 100) foreach println
      // STAGE 2 - WRITESIDE
      use_cases.highest_growing_countries_ranked_by_gdp.stage_2.Writeside.startConsumers(
        pub_sub.interpreter.alpakka.MessageProducer.alpakkaMessageProducer(
          pub_sub.interpreter.alpakka.MessageBrokerRequirements.productionSettings(
            AddYearlyPopulationGrowthTransaction.topic,
            "default"
          )(s, s.dispatcher)
        )
      )

    }

    case class QueryResult(countryCode: String, averageDelta: Long, rank: Long)
    eventually {
      val queryResult: Seq[QueryResult] = sql"""
             with
            top2FastestPopulationGrowingCountries as (
              select countryCode, avg(yearly_population_delta) as averageDelta
              from country_yearly_population_delta
              where year between 2010 and 2018
              group by countryCode
              order by avg(yearly_population_delta) desc
            ),
            country_gdp_ranking as (
              select countryCode, rank
              from country_gdp_ranking
              order by rank asc
            )
          select a.countryCode, a.averageDelta, b.rank
          from
          top2FastestPopulationGrowingCountries as a
          join
          country_gdp_ranking as b
          on a.countryCode = b.countryCode
          LIMIT 10
          ;

           """
        .query[QueryResult]
        .to[List]
        .transact(getTransactor)
        .unsafeRunSync
        .take(10)

      println(queryResult)

      queryResult.head.countryCode should be("CHN")
    }
  }

}
