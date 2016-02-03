package com.persona

import akka.actor.ActorSystem
import akka.http.scaladsl.HttpExt
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer
import com.persona.http.authentication.AuthenticationApi
import com.persona.http.authorization.AuthorizationApi
import com.persona.http.chat.ChatApi
import com.persona.service.authentication.PersonaAuthService
import com.persona.service.authentication.facebook.FacebookAuthService
import com.persona.service.authentication.google.GoogleAuthService
import com.persona.service.authorization.AuthorizationService
import com.persona.service.chat.ChatService
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext

class Bootstrap
  (config: Config, http: HttpExt)
  (implicit actorSystem: ActorSystem, executionContext: ExecutionContext, materializer: Materializer) {

  private[this] val personaConfig = config.getConfig("persona")
  private[this] val googleClientId = personaConfig.getString("google_client_id")

  private[this] val personaAuthService = new PersonaAuthService
  private[this] val facebookAuthService = new FacebookAuthService
  private[this] val googleAuthService = GoogleAuthService("407408718192.apps.googleusercontent.com", http)
  private[this] val authenticationApi = new AuthenticationApi(
    personaAuthService,
    facebookAuthService,
    googleAuthService
  )

  private[this] val authorizationService = new AuthorizationService
  private[this] val authorizationApi = new AuthorizationApi(authorizationService)

  private[this] val chatServce = new ChatService
  private[this] val chatApi = new ChatApi(chatServce)

  val routes = {
    authenticationApi.route ~
    authorizationApi.route ~
    chatApi.route
  }

}
