package com.persona

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.persona.http.RestApi
import com.persona.util.jwk.OpenIdDiscoveryDocumentJwkCache
import com.typesafe.config.ConfigFactory

import scala.io.StdIn

object Main extends App with RestApi {
  private[this] implicit val system = ActorSystem()
  private[this] implicit val executionContext = system.dispatcher
  private[this] implicit val materializer = ActorMaterializer()

  private[this] val config = ConfigFactory.load()
  private[this] val httpConfig = config.getConfig("http")
  private[this] val interface = httpConfig.getString("interface")
  private[this] val port = httpConfig.getInt("port")

  private[this] val http = Http()

  val test = OpenIdDiscoveryDocumentJwkCache(system, http, "https://accounts.google.com/.well-known/openid-configuration")

  test.get.map { jwks =>
    Console.println("Got " + jwks.size + " jwks")
  }

  test.get.map { jwks =>
    Console.println("Got " + jwks.size + " jwks")
  }

  // Start the server
  val binding = http.bindAndHandle(routes, interface, port)

  // Wait for someone to stop the server
  StdIn.readLine()

  // Stop the server
  binding.flatMap(_.unbind())
         .onComplete(_ => system.shutdown())
}
