package use_cases.highest_growing_countries_ranked_by_gdp.stage_1

import cats.effect._
import country_gdp_ranking.application.events.CountryEvents.AddedGDP
import country_gdp_ranking.domain.GDP
import pub_sub.algebra.KafkaKeyValueLike.KafkaKeyValue
import pub_sub.algebra.MessageProducer.ProducedNotification
import pub_sub.algebra.KafkaKeyValueLike
import pub_sub.interpreter.fs2.MessageBrokerRequirements

object ProducerApp extends IOApp {

  def run(args: List[String]): IO[ExitCode] = {

    def isInt(s: String): Boolean = s.matches("""\d+""")

    args match {
      case kafkaServer :: topic :: from :: to :: Nil if isInt(from) && isInt(to) =>
        produce(kafkaServer, topic, from.toInt, to.toInt)
      case _ =>
        println("usage: <topic> <from> <to> -- example: AddedGDP 1 1000")
        IO.pure(ExitCode(1))
    }

  }

  def produce(kafkaServer: String = "0.0.0.0:9092", topic: String, From: Int, To: Int): IO[ExitCode] = {

    import country_gdp_ranking.infrastructure.marshalling._
    import serialization.encode
    val messages: Seq[KafkaKeyValueLike] = (From to To).map { index: Int =>
      val addedGDP = AddedGDP(s"country-${index}", GDP(index))
      KafkaKeyValue(addedGDP.country, encode[AddedGDP](addedGDP))
    }.toSeq

    println(messages)
    pub_sub.interpreter.fs2.MessageProducer
      .fs2MessageProducer(MessageBrokerRequirements.productionSettings)(
        ProducedNotification.print(ProducedNotification.producedNotificationStandardPrintFormat)
      )(topic)(messages)
  }

}
