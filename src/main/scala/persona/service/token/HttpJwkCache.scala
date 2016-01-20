package persona.service.token

import akka.actor._
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model._
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.pattern.ask
import akka.stream.scaladsl.ImplicitMaterializer
import com.nimbusds.jose.jwk.{JWK, JWKSet}

import scala.collection.JavaConversions._
import scala.concurrent.{ExecutionContext, Future}

private object HttpJwkCacheActor {

  object Retrieve

}

private class HttpJwkCacheActor(http: HttpExt, targetUri: String) extends Actor
  with Stash
  with ImplicitMaterializer
  with SprayJsonSupport
  with JwkJsonProtocol {

  // Make a child actor that will send us the http response for targetUri
  context.actorOf(Props(new HttpRequestCache(self, http, targetUri)))

  private[this] implicit val executionContext = context.dispatcher
  private[this] var cache = Set[JWK]()

  // This will be called before the first http response has been received.
  // We will queue up callers so that we can send them an answer once
  // we have received the first http response
  def receive: Receive = {
    case HttpJwkCacheActor.Retrieve =>
      stash()

    case response: HttpResponse if StatusCodes.OK == response.status =>
      handleResponse(response)

    case jwkSet: JWKSet =>
      update(jwkSet)
      unstashAll()
      context.become(initializedReceive)
  }

  // This will be called after the first http response has been received.
  // We will respond to callers immediately
  def initializedReceive: Receive = {
    case HttpJwkCacheActor.Retrieve =>
      retrieve(sender)

    case response: HttpResponse if StatusCodes.OK == response.status =>
      handleResponse(response)

    case jwkSet: JWKSet =>
      update(jwkSet)
  }

  private[this] def retrieve(actor: ActorRef) = {
    actor ! cache
  }

  private[this] def handleResponse(response: HttpResponse) = {
    Unmarshal(response.entity).to[JWKSet].map { jwkSet =>
      self ! jwkSet
    }
  }

  private[this] def update(jwkSet: JWKSet) = {
    cache = jwkSet.getKeys.toSet
  }

}

object HttpJwkCache {

  val RetrieveTimeout = HttpRequestCache.RetrieveTimeout

  def apply(actorSystem: ActorSystem, http: HttpExt, targetUri: String): HttpJwkCache = {
    val actor = actorSystem.actorOf(Props(new HttpJwkCacheActor(http, targetUri)))

    new HttpJwkCache(actor)
  }

}

class HttpJwkCache private(actor: ActorRef) extends ActorWrapper(actor) with JwkCache {

  def get(implicit executionContext: ExecutionContext): Future[Set[JWK]] = {
    implicit val timeout = HttpJwkCache.RetrieveTimeout
    val futureResult = actor ? HttpJwkCacheActor.Retrieve

    futureResult map { result =>
      result.asInstanceOf[Set[JWK]]
    }
  }

}
