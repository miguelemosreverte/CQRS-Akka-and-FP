package pub_sub.interpreter.alpakka

import akka.Done
import akka.actor.ActorSystem
import akka.stream.scaladsl.Source
import akka.kafka.scaladsl.Producer

import scala.util.{Failure, Success}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.{ExecutionContext, Future}
import org.apache.kafka.clients.producer.ProducerRecord
import pub_sub.algebra.MessageProducer.ProducedNotification

object MessageProducer {

  val alpakkaMessageProducer: MessageBrokerRequirements => pub_sub.algebra.MessageProducer.MessageProducer[Future[
    Done
  ]] =
    transactionRequirements =>
      handler =>
        topic =>
          data => {
            implicit val system: ActorSystem = transactionRequirements.system
            implicit val ec: ExecutionContext = transactionRequirements.executionContext

            val publication: Future[Done] = Source(data)
              .map { m =>
                new ProducerRecord[String, String](topic, m.key, m.value)
              }
              .runWith(Producer.plainSink(transactionRequirements.producer))

            publication.onComplete {
              case Success(Done) =>
                data foreach { s =>
                  log.debug(s"""Published $s to $topic""")
                }
                handler(ProducedNotification(topic, data))
              case Failure(t) => log.error("An error has occurred: " + t.getMessage)
            }
            publication
          }

  val log: Logger = LoggerFactory.getLogger(this.getClass)
}
