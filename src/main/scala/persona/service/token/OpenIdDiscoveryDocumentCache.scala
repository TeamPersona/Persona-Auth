package persona.service.token

import akka.actor._
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.ask
import akka.stream.scaladsl.ImplicitMaterializer

import scala.concurrent.{ExecutionContext, Future}

object OpenIdDiscoveryDocumentCache {

  private object Retrieve

  val RetrieveTimeout = HttpRequestCache.RetrieveTimeout

  private class InternalActor
    extends Actor
    with Stash
    with ImplicitMaterializer
    with SprayJsonSupport
    with OpenIdDiscoveryDocumentJsonProtocol {

    private[this] implicit val executionContext = context.dispatcher
    private[this] var document: OpenIdDiscoveryDocument = _

    // This will be called before the first http response has been received.
    // We will queue up callers so that we can send them an answer once
    // we have received the first http response
    def receive: Receive = {
      case OpenIdDiscoveryDocumentCache.Retrieve =>
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
      case OpenIdDiscoveryDocumentCache.Retrieve =>
        retrieve(sender)

      case response: HttpResponse if StatusCodes.OK == response.status =>
        handleResponse(response)

      case openIdDiscoverDocument: OpenIdDiscoveryDocument =>
        update(openIdDiscoverDocument)
    }

    private[this] def retrieve(actor: ActorRef) = {
      actor ! document
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

  def apply(
    actorSystem: ActorSystem,
    scheduler: Scheduler,
    http: HttpExt,
    targetUri: String): OpenIdDiscoveryDocumentCache = {

    val internalActor = actorSystem.actorOf(Props(new OpenIdDiscoveryDocumentCache.InternalActor))
    val requestCache = actorSystem.actorOf(Props(new HttpRequestCache(internalActor, scheduler, http, targetUri)))

    new OpenIdDiscoveryDocumentCache(internalActor, requestCache)
  }

}

// The requestCache is passed in so that it doesn't get garbage collected
class OpenIdDiscoveryDocumentCache private(internalActor: ActorRef, requestCache: ActorRef) {

  def get(implicit executionContext: ExecutionContext): Future[OpenIdDiscoveryDocument] = {
    implicit val timeout = OpenIdDiscoveryDocumentCache.RetrieveTimeout
    val futureResult = internalActor ? OpenIdDiscoveryDocumentCache.Retrieve

    futureResult map { result =>
      result.asInstanceOf[OpenIdDiscoveryDocument]
    }
  }

}
