package com.persona.util.jwk

import akka.actor._
import akka.http.scaladsl.HttpExt
import akka.pattern.ask
import akka.util.Timeout
import com.nimbusds.jose.jwk.JWK
import com.persona.util.actor.ActorWrapper
import com.persona.util.openid.{OpenIdDiscoveryDocumentCache, OpenIdDiscoveryDocument}

import scala.concurrent.{ExecutionContext, Future}

private object OpenIdDiscoveryDocumentJwkCacheActor {

  private case class HandleDocument(actor: ActorRef, document: OpenIdDiscoveryDocument)
  object Retrieve

}

private class OpenIdDiscoveryDocumentJwkCacheActor(
  documentCache: OpenIdDiscoveryDocumentCache,
  http: HttpExt)
  extends Actor {

  private[this] implicit val executionContext = context.dispatcher
  private[this] var documentOption: Option[OpenIdDiscoveryDocument] = None
  private[this] var jwkCache: HttpJwkCache = _

  def receive: Receive = {
    case OpenIdDiscoveryDocumentJwkCacheActor.Retrieve =>
      // Need to keep a local copy of the sender, since it
      // is only valid in the current context
      val actor = sender

      documentCache.get.map { document =>
        self ! OpenIdDiscoveryDocumentJwkCacheActor.HandleDocument(actor, document)
      }

    case OpenIdDiscoveryDocumentJwkCacheActor.HandleDocument(actor, document) =>
      tryUpdate(document)

      jwkCache.get.map { jwks =>
        actor ! jwks
      }
  }

  private[this] def tryUpdate(openIdDiscoveryDocument: OpenIdDiscoveryDocument) = {
    val (updatedDocumentOption, updatedJwkCache) = documentOption.map { existingDocument =>
      // Check and see if the document has changed
      if(existingDocument != openIdDiscoveryDocument) {
        // Document has changed - kill the old cache
        jwkCache.stop()

        // Now create the new cache
        update(openIdDiscoveryDocument)
      } else {
        // Document hasn't changed
        (documentOption, jwkCache)
      }
    } getOrElse {
      // First document we've gotten
      update(openIdDiscoveryDocument)
    }

    documentOption = updatedDocumentOption
    jwkCache = updatedJwkCache
  }

  private[this] def update(openIdDiscoveryDocument: OpenIdDiscoveryDocument) = {
    val updatedDocumentOption = Some(openIdDiscoveryDocument)
    val updatedJwkCache = HttpJwkCache(context.system, http, openIdDiscoveryDocument.jwksUri)

    (updatedDocumentOption, updatedJwkCache)
  }

}

object OpenIdDiscoveryDocumentJwkCache {

  val RetrieveTimeout = Timeout(
    HttpJwkCache.RetrieveTimeout.duration + OpenIdDiscoveryDocumentCache.RetrieveTimeout.duration
  )

  def apply(actorSystem: ActorSystem, documentCache: OpenIdDiscoveryDocumentCache, http: HttpExt): OpenIdDiscoveryDocumentJwkCache = {
    val internalActor = actorSystem.actorOf(Props(new OpenIdDiscoveryDocumentJwkCacheActor(documentCache, http)))

    new OpenIdDiscoveryDocumentJwkCache(internalActor)
  }

}

class OpenIdDiscoveryDocumentJwkCache private(actor: ActorRef) extends ActorWrapper(actor) with JwkCache {

  def get(implicit executionContext: ExecutionContext): Future[Set[JWK]] = {
    implicit val timeout = OpenIdDiscoveryDocumentJwkCache.RetrieveTimeout
    val futureResult = actor ? OpenIdDiscoveryDocumentJwkCacheActor.Retrieve

    futureResult.map { result =>
      result.asInstanceOf[Set[JWK]]
    }
  }

}
