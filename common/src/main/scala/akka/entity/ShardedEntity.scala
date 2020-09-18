package akka.entity

import akka.actor.{ActorRef, ActorSystem, Props}
import akka.cluster.sharding.{ClusterSharding, ClusterShardingSettings, ShardRegion}

trait ShardedEntity {
  import ShardedEntity._
  def props: Props
  def start(
      implicit
      system: ActorSystem
  ): ActorRef = ClusterSharding(system).start(
    typeName = this.getClass.getSimpleName,
    entityProps = props,
    settings = ClusterShardingSettings(system),
    extractEntityId = extractEntityId,
    extractShardId = extractShardId(3 * 10)
  )
}

object ShardedEntity {
  def extractEntityId: ShardRegion.ExtractEntityId = {
    case s: Sharded => (s.entityId, s)
  }
  def extractShardId(numberOfShards: Int): ShardRegion.ExtractShardId = {
    case s: Sharded => (s.shardId.hashCode % numberOfShards).toString
  }
  trait Sharded {
    def entityId: String
    def shardId: String = entityId
  }
}
