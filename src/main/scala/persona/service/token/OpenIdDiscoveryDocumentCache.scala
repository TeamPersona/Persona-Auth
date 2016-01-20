package persona.service.token

import akka.actor._
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.ask
import akka.stream.scaladsl.ImplicitMaterializer

import scala.concurrent.{ExecutionContext, Future}

private object OpenIdDiscoveryDocumentCacheActor {

  object Retrieve

}

private class OpenIdDiscoveryDocumentCacheActor(http: HttpExt, targetUri: String) extends Actor
  with Stash
  with ImplicitMaterializer
  with SprayJsonSupport
  with OpenIdDiscoveryDocumentJsonProtocol {

  // Make a child actor that will send us the http response for targetUri
  context.actorOf(Props(new HttpRequestCache(self, http, targetUri)))

  private[this] implicit val executionContext = context.dispatcher
  private[this] var document: OpenIdDiscoveryDocument = _

  // This will be called before the first http response has been received.
  // We will queue up callers so that we can send them an answer once
  // we have received the first http response
  def receive: Receive = {
    case OpenIdDiscoveryDocumentCacheActor.Retrieve =>
      stash()

    case response: HttpResponse if StatusCodes.OK == response.status =>
      handleResponse(response)

    case openIdDiscoverDocument: OpenIdDiscoveryDocument =>
      update(openIdDiscoverDocument)
      unstashAll()
      context.become(initializedReceive)
  }

  // This will be called after the first http response has been received.
  // We will respond to callers immediately
  def initializedReceive: Receive = {
    case OpenIdDiscoveryDocumentCacheActor.Retrieve =>
      sender ! document

    case response: HttpResponse if StatusCodes.OK == response.status =>
      handleResponse(response)

    case openIdDiscoverDocument: OpenIdDiscoveryDocument =>
      update(openIdDiscoverDocument)
  }

  private[this] def handleResponse(response: HttpResponse) = {
    Unmarshal(response.entity).to[OpenIdDiscoveryDocument].map { openIdDiscoveryDocument =>
      self ! openIdDiscoveryDocument
    }
  }

  private[this] def update(openIdDiscoveryDocument: OpenIdDiscoveryDocument) = {
    document = openIdDiscoveryDocument
  }

}

object OpenIdDiscoveryDocumentCache {

  val RetrieveTimeout = HttpRequestCache.RetrieveTimeout

  def apply(actorSystem: ActorSystem, http: HttpExt, targetUri: String): OpenIdDiscoveryDocumentCache = {
    val actor = actorSystem.actorOf(Props(new OpenIdDiscoveryDocumentCacheActor(http, targetUri)))

    new OpenIdDiscoveryDocumentCache(actor)
  }

}

class OpenIdDiscoveryDocumentCache private(actor: ActorRef) extends ActorWrapper(actor) {

  def get(implicit executionContext: ExecutionContext): Future[OpenIdDiscoveryDocument] = {
    implicit val timeout = OpenIdDiscoveryDocumentCache.RetrieveTimeout
    val futureResult = actor ? OpenIdDiscoveryDocumentCacheActor.Retrieve

    futureResult map { result =>
      result.asInstanceOf[OpenIdDiscoveryDocument]
    }
  }

}
