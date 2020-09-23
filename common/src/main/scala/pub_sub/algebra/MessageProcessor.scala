package pub_sub.algebra

import pub_sub.algebra.KafkaKeyValueLike.KafkaKeyValue

object MessageProcessor {

  type ConsumerGroup = String
  type Topic = String
  type Algorithm[Output] = KafkaKeyValue => Either[Throwable, Output]
  type MessageProcessor[MessageProcessorOutput, AlgorithmOutput] =
    ConsumerGroup => Topic => Algorithm[AlgorithmOutput] => MessageProcessorOutput

}
