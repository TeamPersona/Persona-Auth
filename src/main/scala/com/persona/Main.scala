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
  val testIdToken = SignedJWT.parse("eyJhbGciOiJSUzI1NiIsImtpZCI6IjFmYmU4OTA5M2JkZTk1NTc1YTMwMDY1OTlmMWExOWQyMzViMzgzYjcifQ.eyJpc3MiOiJhY2NvdW50cy5nb29nbGUuY29tIiwiYXRfaGFzaCI6IjMzazRjRHhZc0JTeHljNkppNzFFWWciLCJhdWQiOiI0MDc0MDg3MTgxOTIuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMDA4NjU5NzMyMjk2NjA1OTczMTQiLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwiYXpwIjoiNDA3NDA4NzE4MTkyLmFwcHMuZ29vZ2xldXNlcmNvbnRlbnQuY29tIiwiZW1haWwiOiJ0YXlsb3Iuc3RhcmswM0BnbWFpbC5jb20iLCJpYXQiOjE0NTM2NzI0NzIsImV4cCI6MTQ1MzY3NjA3Mn0.jnsezmvrRvDiwOG2HnouEKJIoAUb6tlZ9RVPQ1Uaoewk2kTm-MM5n9zoVmb2JUqjTsrxgXzk5KSaBdOVY-otI_447mhG0icDfa0mVB8cLxK0tYHtHt_Gqk8JBnme80MWY_Vm-rMj0QC2sVQb5Yaqpt6x2E_RauxFHPiWi359IbXk9oGpLByizdHR0JCqG5tJRdJiQTb8BSlkWfTTcYarMEyA3M-wT0XlosuavErAf23C8w19_LDn5TDjOSllaZ0Q0IuOwCkqkODBqpQPaV_tz-pSj6nUKk1T9qz2hK9rfULK1MIKuV5OAHTEL_eAdJQ__H0jsHbdbuHydzUwyhqXEA")

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
