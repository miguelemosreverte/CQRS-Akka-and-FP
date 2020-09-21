package pub_sub.interpreter.utils

import com.typesafe.config.Config

import scala.util.Try

class KafkaConfig(config: Config) {
  lazy val KAFKA_BROKER: String = Try { config.getString("kafka.brokers") }.getOrElse("0.0.0.0:9092")

}
