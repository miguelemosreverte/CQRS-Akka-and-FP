package pub_sub.interpreter.utils

import akka.actor.{Actor, ActorLogging}
import akka.cluster.sharding.external._
import akka.cluster.{Cluster => CCluster}
import akka.kafka.{TopicPartitionsAssigned, TopicPartitionsRevoked}

import scala.concurrent.duration._
import scala.util.Failure
import scala.util.Success

class TopicListener(typeKeyName: String) extends Actor with ActorLogging {

  val system = context.system
  implicit val ec = system.dispatcher

  val shardAllocationClient = ExternalShardAllocation(system).clientFor(typeKeyName)
  system.scheduler.scheduleAtFixedRate(10.seconds, 20.seconds) { () =>
    shardAllocationClient.shardLocations().onComplete {
      case Success(shardLocations) =>
      // ctx.log.info("Current shard locations {}", shardLocations.locations)
      case Failure(t) =>
        log.error("failed to get shard locations", t)
    }
  }
  val address = CCluster(system).selfMember.address

  override def receive: Receive = {
    case TopicPartitionsAssigned(_, partitions) =>
      partitions.foreach(partition => {
        // ctx.log.info("Partition [{}] assigned to current node. Updating shard allocation", partition.partition())
        // kafka partition becomes the akka shard
        val done = shardAllocationClient.updateShardLocation(partition.partition().toString, address)
        done.onComplete { result =>
          // ctx.log.info("Result for updating shard {}: {}", partition, result)
        }
      })
    case TopicPartitionsRevoked(_, topicPartitions) =>
      log.warning("Partitions [{}] revoked from current node. New location will update shard allocation",
                  topicPartitions.mkString(","))

  }
}
