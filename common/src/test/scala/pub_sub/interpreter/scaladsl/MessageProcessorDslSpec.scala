package pub_sub.interpreter.scaladsl

import play.api.libs.json.Json
import domain_driven_design.cqrs.Command
import org.scalatest.flatspec.AnyFlatSpec
import pub_sub.algebra.KafkaKeyValueLike.KafkaKeyValue
import pub_sub.algebra.MessageProducer
import pub_sub.algebra.MessageProducer.ProducedNotification
import pub_sub.interpreter.fs2.MessageBrokerRequirements

object MessageProcessorDslSpec {
  case class ExampleCommand(entityId: String, body: String) extends Command
  val exampleCommand = ExampleCommand("Argentina", "is facing some problems.")
  implicit val ExampleCommandF = Json.format[ExampleCommand]
  implicit def EntityAsKafkaValue(exampleCommand: ExampleCommand): KafkaKeyValue =
    KafkaKeyValue(exampleCommand.entityId, serialization.encode(exampleCommand))
}

class MessageProcessorDslSpec extends AnyFlatSpec {
  "the following statement" should "compile" in {
    import MessageProcessorDslSpec._
    // format: off
    import pub_sub.scaladsl.MessageProducerDSL._
    implicit val r: MessageBrokerRequirements = null
    MessageProducer
      .fs2
      .withLogger
      .produce(Seq(exampleCommand))

  }
}
