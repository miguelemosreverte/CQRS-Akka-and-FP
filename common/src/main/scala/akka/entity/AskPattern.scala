package akka.entity

import akka.util.Timeout

import scala.concurrent.Future
import scala.reflect.ClassTag

object AskPattern {

  /*
  This mechanism allows the user to express in the following manner:

  for {
    _: Response.SuccessProcessing <- actorRef.ask[Done](command)
  }

  However AkkaClassic does not provide type guarantees, so if the Actor
  does not answer the expected type the Future will fail.

   */
  implicit class AkkaClassicTypedAsk(actorRef: akka.actor.ActorRef) {

    import scala.concurrent.duration._

    implicit val timeout: Timeout = Timeout(20 seconds)

    import akka.pattern.{ask => classicAsk}

    def ask[Response: ClassTag](command: Any): Future[Response] =
      (actorRef ? command).mapTo[Response]
  }

}
