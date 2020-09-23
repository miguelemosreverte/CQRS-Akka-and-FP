package akka.http

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.settings.ConnectionPoolSettings
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.{Materializer, SystemMaterializer}
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}

class AkkaHttpClient(
    implicit
    system: ActorSystem,
    ec: ExecutionContext
) {
  implicit private val m: Materializer = SystemMaterializer(system).materializer
  private val connectionPoolSettings: ConnectionPoolSettings = ConnectionPoolSettings(system)
  private val logger = LoggerFactory.getLogger(this.getClass)

  def get(uri: String): Future[HttpResponse] = call(HttpMethods.GET)(uri)

  def post(uri: String, payload: String): Future[HttpResponse] =
    call(HttpMethods.POST)(uri)
      .map(withPayload(payload))

  def put(uri: String, payload: String): Future[HttpResponse] =
    call(HttpMethods.PUT)(uri)
      .map(withPayload(payload))

  def delete(uri: String): Future[HttpResponse] = call(HttpMethods.DELETE)(uri)

  def patch(uri: String): Future[HttpResponse] = call(HttpMethods.PATCH)(uri)

  def withPayload(payload: String)(response: HttpResponse): HttpResponse =
    response.withEntity(
      HttpEntity(ContentTypes.`application/json`, payload)
    )

  private def call(method: HttpMethod)(uri: String): Future[HttpResponse] =
    Http()
      .singleRequest(
        request = HttpRequest(
          method = method,
          uri = uri
        ),
        settings = connectionPoolSettings
      )
      .map { response =>
        logger.debug(
          "[HttpClientResult]",
          "method" -> method,
          "uri" -> uri,
          "response" -> response
        )
        response
      }
}

object AkkaHttpClient {
  def as[A](response: HttpResponse)(implicit materializer: Materializer, ev: Unmarshaller[HttpResponse, A]): Future[A] =
    Unmarshal(response).to[A]
}
