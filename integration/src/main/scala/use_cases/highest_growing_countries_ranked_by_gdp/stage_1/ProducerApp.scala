package use_cases.highest_growing_countries_ranked_by_gdp.stage_1

import cats.effect._
import pub_sub.scaladsl.MessageProducerDSL._
import pub_sub.algebra.KafkaKeyValueLike.KafkaKeyValue
import pub_sub.interpreter.fs2.MessageBrokerRequirements
import pub_sub.algebra.{KafkaKeyValueLike, MessageProducer}
import entities.{CountryGrossDomesticProductGlobalRanking, CountryYearlyTotalPopulation}
import pub_sub.algebra.MessageProducer.MessageProducer

object ProducerApp extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {

    def isInt(s: String): Boolean = s.matches("""\d+""")

    args match {
      case kafkaServer :: topic :: from :: to :: Nil if isInt(from) && isInt(to) =>
        val messageProducer: MessageProducer[IO[ExitCode]] =
          MessageProducer
            .fs2(MessageBrokerRequirements.productionSettings)
        produce(messageProducer)(kafkaServer, topic, from.toInt, to.toInt)
      case _ =>
        println("usage: <topic> <from> <to> -- example: CountryGrossDomesticProductGlobalRanking 1 1000")
        IO.pure(ExitCode(1))
    }
  }

  def produce[Out](
      messageProducer: MessageProducer[Out]
  )(kafkaServer: String = "0.0.0.0:9092", topic: String, From: Int, To: Int): Out = {

    println(s"FROM ${From} to ${To}")
    val messages: Seq[KafkaKeyValueLike] = topic match {
      case s"CountryGrossDomesticProductGlobalRanking" =>
        import entities.marshalling._
        import serialization.encode
        implicit val toKafkaKeyValue: CountryGrossDomesticProductGlobalRanking => KafkaKeyValue =
          entity => KafkaKeyValue(entity.country, encode[CountryGrossDomesticProductGlobalRanking](entity))

        val messages: Seq[CountryGrossDomesticProductGlobalRanking] = (From to To).map { index: Int =>
          CountryGrossDomesticProductGlobalRanking(s"country-${index}", index)
        }
        messages map toKafkaKeyValue
      case s"CountryYearlyTotalPopulation" =>
        import entities.marshalling._
        import serialization.encode
        implicit val toKafkaKeyValue: CountryYearlyTotalPopulation => KafkaKeyValue =
          entity => KafkaKeyValue(entity.country, encode[CountryYearlyTotalPopulation](entity))

        val messages: Seq[CountryYearlyTotalPopulation] = (From to To).map { index: Int =>
          CountryYearlyTotalPopulation(s"country-${index}", index, index)
        }
        messages map toKafkaKeyValue
    }

    messageProducer.withLogger
      .produce(messages, topic)

  }

}
