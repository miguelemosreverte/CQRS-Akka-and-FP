package pub_sub.algebra

import scala.concurrent.Future
import akka.Done
import akka.stream.KillSwitch

/*
This mechanism allows the user to process messages from the message bus

It delegates onto the user the deserialization of the messages

It returns a tuple which contains

1. KillSwitch for the user to stop the message stream
2. Future[Done] to handle the correct stream termination
 */
trait MessageProcessor {

  type MessageProcessorKillSwitch <: KillSwitch
  val topicName: String
  val consumerGroup: String
  def run(
      algorithm: String => Either[Throwable, Future[Done]]
  ): (Option[MessageProcessorKillSwitch], Future[Done])

}
