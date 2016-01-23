package com.persona.service.authentication.google

import akka.actor.{Props, ActorSystem, ActorRef, Actor}
import akka.http.scaladsl.HttpExt
import akka.pattern.ask
import akka.util.Timeout
import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jwt.JWT
import com.nimbusds.oauth2.sdk.id.{ClientID, Issuer}
import com.nimbusds.openid.connect.sdk.validators.IDTokenValidator
import com.persona.util.actor.ActorWrapper
import com.persona.util.jwk.OpenIdDiscoveryDocumentJwkCache
import com.persona.util.openid.OpenIdDiscoveryDocumentCache

import scala.collection.JavaConversions._
import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

private object GoogleAuthServiceActor {

  case class Authenticate(idToken: JWT)

}

private class GoogleAuthServiceActor(
  discoveryDocumentUrl: String,
  clientID: String,
  http: HttpExt) extends Actor {

  private[this] implicit val executionContext = context.dispatcher
  private[this] val discoveryDocumentCache = OpenIdDiscoveryDocumentCache(context.system, http, discoveryDocumentUrl)
  private[this] val jwkCache = OpenIdDiscoveryDocumentJwkCache(context.system, discoveryDocumentCache, http)

  def receive: Receive = {
    case GoogleAuthServiceActor.Authenticate(idToken) =>
      val actor = sender

      discoveryDocumentCache.get.map { discoveryDocument =>
        jwkCache.get.map { jwks =>
          val v1tokenValidator = new IDTokenValidator(
            new Issuer("accounts.google.com"),
            new ClientID(clientID),
            JWSAlgorithm.RS256,
            new JWKSet(jwks.toList)
          )

          val v1validation = Try(v1tokenValidator.validate(idToken, null))

          if(v1validation.isSuccess) {
            actor ! true
          } else {
            val v2tokenValidator = new IDTokenValidator(
              new Issuer(discoveryDocument.issuer),
              new ClientID(clientID),
              JWSAlgorithm.RS256,
              new JWKSet(jwks.toList)
            )

            val v2validation = Try(v2tokenValidator.validate(idToken, null))

            actor ! v2validation.isSuccess
          }
        }
      }
  }

}

object GoogleAuthService {

  val AuthenticateTimeout = Timeout(60.seconds)
  private val GoogleDiscoveryDocumentUrl = "https://accounts.google.com/.well-known/openid-configuration"

  def apply(actorSystem: ActorSystem, clientID: String, http: HttpExt): GoogleAuthService = {
    val actor = actorSystem.actorOf(
      Props(
        new GoogleAuthServiceActor(GoogleAuthService.GoogleDiscoveryDocumentUrl, clientID, http)
      )
    )

    new GoogleAuthService(actor)
  }

}

class GoogleAuthService private(actor: ActorRef) extends ActorWrapper(actor) {

  def authenticate(idToken: JWT)(implicit executionContext: ExecutionContext): Future[Boolean] = {
    implicit val timeout = GoogleAuthService.AuthenticateTimeout
    val futureResult = actor ? GoogleAuthServiceActor.Authenticate(idToken)

    futureResult.map { result =>
      result.asInstanceOf[Boolean]
    }
  }

}
