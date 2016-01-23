package com.persona

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import com.nimbusds.jwt.SignedJWT
import com.persona.http.RestApi
import com.persona.service.authentication.google.GoogleAuthService
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

  private[this] val personaConfig = config.getConfig("persona")
  private[this] val googleClientId = personaConfig.getString("google_client_id")

  private[this] val http = Http()

  val test = GoogleAuthService(system, "407408718192.apps.googleusercontent.com", http)
  val testIdToken = SignedJWT.parse("eyJhbGciOiJSUzI1NiIsImtpZCI6ImEzMDVkZGIxZjMzMDhlODM5MDkyNmVlNzAwM2I2OWQxYzFjZjA0NTUifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiYXRfaGFzaCI6IlBqMzQ4azUtYkdod3FKcHl3eV9yLWciLCJhdWQiOiI0MDc0MDg3MTgxOTIuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDA4NjU5NzMyMjk2NjA1OTczMTQiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXpwIjoiNDA3NDA4NzE4MTkyLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiZW1haWwiOiJ0YXlsb3Iuc3RhcmswM0BnbWFpbC5jb20iLCJpYXQiOjE0NTM1MTQwMTMsImV4cCI6MTQ1MzUxNzYxM30.pcWKzLSB9_mT1D9T2C_-8IpAwywSQHxMA0sJhDkAx2Yfvl012SWntGkD0dxNAaNmCqi3F92L1a4X9r9oUWr-ooR3INtD1lr5mwMSkGIAR9TDDCxk3e7jBVdI5XRitfGeSz2EG4HwB2gBmsaAz9q91VSOfGHoVaNJYMLf7dSb4sOe6EBuGpS3rACuUV0S0T1pGoVaodmyBZmSyTkE7WCRK-cnCRNP6x595Y8DXdHTgo5Yzp7ROYzmNAnJf40n8Pkah--x6xiGgY3pg_79I0fHs8n3qwPIzeoWMYg5MVzFEAUTVc59_cuapTM-ubiGZwtLzBoRAVK99fyyKBmRYe5vYA")

  test.authenticate(testIdToken).map { result =>
    Console.println("Got response " + result)
  }

  // Start the server
  val binding = http.bindAndHandle(routes, interface, port)

  // Wait for someone to stop the server
  StdIn.readLine()

  // Stop the server
  binding.flatMap(_.unbind())
         .onComplete(_ => system.shutdown())
}
