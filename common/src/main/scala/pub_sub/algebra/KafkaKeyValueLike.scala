package pub_sub.algebra

trait KafkaKeyValueLike {
  val key: String
  val value: String
}
object KafkaKeyValueLike {
  case class KafkaKeyValue(key: String, value: String) extends KafkaKeyValueLike {
    val aggregateRoot: String = key
    val json: String = value
  }
}
