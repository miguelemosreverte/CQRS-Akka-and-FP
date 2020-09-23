package akka.entity

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}
import akka.local_processing.LocalizedProcessingMessageExtractor

// TODO use FP instead of OOP
trait ShardedEntity[Requirements] {
  import ShardedEntity._
  def props(requirements: Requirements): Props
  def start(requirements: Requirements)(implicit
      system: ActorSystem
  ): ActorRef = ClusterSharding(system).start(
    typeName = this.getClass.getSimpleName,
    entityProps = props(requirements),
    settings = ClusterShardingSettings(system),
    extractEntityId = extractEntityId,
    extractShardId = extractShardId(3 * 10)
  )
}

object ShardedEntity {
  def extractEntityId: ShardRegion.ExtractEntityId = { case s: Sharded =>
    (s.entityId, s)
  }
  def extractShardId(numberOfShards: Int): ShardRegion.ExtractShardId = { case s: Sharded =>
    new LocalizedProcessingMessageExtractor(numberOfShards * 10).shardId(s.shardId)
  //(s.shardId.hashCode % numberOfShards).toString
  }
  trait Sharded {
    def entityId: String
    def shardId: String = entityId
  }
}
