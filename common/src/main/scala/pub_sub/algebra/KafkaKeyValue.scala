package pub_sub.algebra

case class KafkaKeyValue(key: String, value: String) {
  val aggregateRoot: String = key
  val json: String = value
}
