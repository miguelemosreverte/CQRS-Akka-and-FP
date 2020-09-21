package main
import cats.data.State
import cats.effect._
import cats.effect.concurrent.Ref
import cats.implicits._
import fs2.kafka._
import fs2.{Pipe, Stream}
import main.Main.Random.Seed
import play.api.libs.json.Json
import pub_sub.algebra.KafkaKeyValueLike
import pub_sub.algebra.MessageProducer.ProducedNotification
import pub_sub.interpreter.fs2.MessageBrokerRequirements

object Main extends IOApp {
  object Random {
    final case class Seed(long: Long) {
      def next: Seed = Seed(long * 6364136223846793005L + 1442695040888963407L)
    }
  }

  val nextGDP: State[Seed, GDP with KafkaKeyValueLike] = State { seed: Seed =>
    val next = seed.next
    val l = next.long
    (next, new GDP(l.toString, l.toInt) with KafkaKeyValueLike {
      override val key: String = this.country
      override val value: String = GDP.serialize(this)
    })
  }

  def run(args: List[String]): IO[ExitCode] = {
    import pub_sub.interpreter.fs2.MessageProducer.fs2MessageProducer
    val messageStream = randomMessages(Random.Seed(1L))(nextGDP).take(12)
    val messages: Seq[KafkaKeyValueLike] = messageStream.compile.toList.unsafeRunSync()
    fs2MessageProducer(MessageBrokerRequirements.productionSettings)(messages)("topic")(
      ProducedNotification.print(ProducedNotification.producedNotificationStandardPrintFormat)
    )

  }

  def randomMessages[Message](
      seed: Random.Seed
  )(next: State[Seed, Message with KafkaKeyValueLike]): Stream[IO, KafkaKeyValueLike] =
    for {
      ref <- Stream.eval(Ref.of[IO, Random.Seed](seed))
      message <- Stream.repeatEval(ref.modifyState(next))
    } yield message

  case class GDP(country: String, rank: Int) {
    def toJson = GDP.serialize(this)
  }

  object GDP {
    import serialization.encode
    implicit val GDPF = Json.format[GDP]
    def serialize(gdp: GDP): String = encode(gdp)
  }
}
