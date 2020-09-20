package akka.local_processing

import java.nio.charset.StandardCharsets

import akka.cluster.sharding.typed.ShardingMessageExtractor
import akka.entity.ShardedEntity.Sharded
import org.apache.kafka.common.utils.Utils

class LocalizedProcessingMessageExtractor[ActorMessages <: Sharded](
    nrKafkaPartitions: Int
) extends ShardingMessageExtractor[ActorMessages, ActorMessages] {
  override def entityId(message: ActorMessages): String = message.entityId

  override def shardId(entityId: String): String = {
    LocalizedProcessingMessageExtractor.shardAndPartition(entityId, nrKafkaPartitions).toString
  }

  override def unwrapMessage(message: ActorMessages): ActorMessages = message
}

object LocalizedProcessingMessageExtractor {
  /*

        Keeping the processing local
        means that we process messages
        on the same node we receive the messages
        from Kafka.

        This gives us:
        responsive:
        - diminished latency
        resilience:
        - no networking failure point

   */
  def shardAndPartition(entityId: String, nr_partitions: Int): Int =
    Utils.toPositive(Utils.murmur2(entityId.getBytes(StandardCharsets.UTF_8))) % nr_partitions
}
