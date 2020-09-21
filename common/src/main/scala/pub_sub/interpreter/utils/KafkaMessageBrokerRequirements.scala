package pub_sub.interpreter.utils

import akka.actor.Props
import akka.kafka.{ConsumerSettings, ProducerSettings}
import com.typesafe.config.ConfigFactory
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.concurrent.ExecutionContext

case class KafkaMessageBrokerRequirements(
    system: akka.actor.ActorSystem,
    executionContext: ExecutionContext,
    rebalancerListener: akka.actor.ActorRef,
    consumer: ConsumerSettings[String, String],
    producer: ProducerSettings[String, String]
)

object KafkaMessageBrokerRequirements {

  private val config = ConfigFactory.load()
  private val appConfig = new KafkaConfig(config)
  val bootstrapServers: String = appConfig.KAFKA_BROKER

  private implicit def consumerSettings(consumerGroup: String)(
      implicit
      system: akka.actor.ActorSystem
  ): ConsumerSettings[String, String] =
    ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withGroupId(consumerGroup)
      .withBootstrapServers(bootstrapServers)

  private implicit def producerSettings(
      implicit
      system: akka.actor.ActorSystem
  ): ProducerSettings[String, String] =
    ProducerSettings(system, new StringSerializer, new StringSerializer)
      .withBootstrapServers(bootstrapServers)

  def productionSettings(topicName: String, consumerGroup: String)(
      implicit
      system: akka.actor.ActorSystem,
      executionContext: ExecutionContext
  ) =
    KafkaMessageBrokerRequirements(
      system,
      executionContext,
      system.actorOf(Props(new TopicListener(topicName))),
      consumerSettings(consumerGroup),
      producerSettings
    )
}
