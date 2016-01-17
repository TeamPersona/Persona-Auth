package persona.service.token

import akka.actor._
import akka.http.scaladsl.HttpExt
import akka.pattern.ask
import akka.util.Timeout
import com.nimbusds.jose.jwk.JWK
import persona.service.token.OpenIdDiscoveryDocumentJwkCache.{Retrieve, RetrieveTimeout}

import scala.concurrent.{ExecutionContext, Future}

object OpenIdDiscoveryDocumentJwkCache {

  private case class HandleDocument(actor: ActorRef, document: OpenIdDiscoveryDocument)
  private object Retrieve

  val RetrieveTimeout = Timeout(
    HttpJwkCache.RetrieveTimeout.duration + OpenIdDiscoveryDocumentCache.RetrieveTimeout.duration)

  private class InternalActor(
    actorSystem: ActorSystem,
    scheduler: Scheduler,
    http: HttpExt,
    targetUri: String)
    extends Actor {

    private[this] implicit val executionContext = context.dispatcher
    private[this] val documentCache = OpenIdDiscoveryDocumentCache(actorSystem, scheduler, http, targetUri)
    private[this] var documentOption: Option[OpenIdDiscoveryDocument] = None
    private[this] var jwkCache: JwkCache = _

    def receive: Receive = {
      case Retrieve =>
        // Need to keep a local copy of the sender, since it
        // is only valid in the current context
        val actor = sender

        documentCache.get.map { document =>
          self ! HandleDocument(actor, document)
        }

      case HandleDocument(actor, document) =>
        tryUpdate(document)

        jwkCache.get.map { jwks =>
          actor ! jwks
        }
    }

    private[this] def tryUpdate(openIdDiscoveryDocument: OpenIdDiscoveryDocument) = {
      if(documentOption.isDefined) {
        // Check and see if the document has changed
        documentOption.foreach { existingDocument =>
          if(existingDocument != openIdDiscoveryDocument) {
            update(openIdDiscoveryDocument)
          }
        }
      } else {
        // First document we've gotten
        update(openIdDiscoveryDocument)
      }
    }

    private[this] def update(openIdDiscoveryDocument: OpenIdDiscoveryDocument) = {
      documentOption = Some(openIdDiscoveryDocument)
      jwkCache = HttpJwkCache(actorSystem, scheduler, http, openIdDiscoveryDocument.jwksUri)
    }

  }

  def apply(
    actorSystem: ActorSystem,
    scheduler: Scheduler,
    http: HttpExt,
    targetUri: String): OpenIdDiscoveryDocumentJwkCache = {

    val internalActor = actorSystem.actorOf(Props(new InternalActor(actorSystem, scheduler, http, targetUri)))

    new OpenIdDiscoveryDocumentJwkCache(internalActor)
  }

}

class OpenIdDiscoveryDocumentJwkCache private(internalActor: ActorRef) extends JwkCache {

  def get(implicit executionContext: ExecutionContext): Future[Set[JWK]] = {
    implicit val timeout = RetrieveTimeout
    val futureResult = internalActor ? Retrieve

    futureResult map { result =>
      result.asInstanceOf[Set[JWK]]
    }
  }

}
