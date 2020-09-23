package akka.http

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

import akka.actor.ActorSystem
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.cluster.ClusterEvent.MemberUp
import akka.http.scaladsl.Http
import akka.http.scaladsl.server._

object AkkaHttpServer {

  case class StopAkkaHttpServer()

  def start(routes: Route, host: String, port: Int)(ctx: ActorSystem): Unit = {

    implicit val system: ActorSystem = ctx
    implicit val ec: ExecutionContext = system.dispatcher

    Http()(system)
      .bindAndHandle(routes, host, port)
      .onComplete {
        case Success(bound) =>
          ctx.log.info(
            s"Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/"
          )
        case Failure(e) =>
          ctx.log.error("Server could not start!")
          e.printStackTrace()
      }

  }
}
