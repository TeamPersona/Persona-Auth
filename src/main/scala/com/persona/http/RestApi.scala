package com.persona.http

import akka.http.scaladsl.server.Directives._
import com.persona.http.authentication.AuthenticationApi
import com.persona.http.authorization.AuthorizationApi
import com.persona.service.authentication.PersonaAuthService
import com.persona.service.authentication.facebook.FacebookAuthService
import com.persona.service.authentication.google.GoogleAuthService
import com.persona.service.authorization.AuthorizationService

trait RestApi {
  /*
  private[this] val personaAuthService = new PersonaAuthService
  private[this] val facebookAuthService = new FacebookAuthService
  private[this] val googleAuthService = new GoogleAuthService
  private[this] val authenticationApi = new AuthenticationApi(personaAuthService,
                                                              facebookAuthService,
                                                              googleAuthService)
  */
  private[this] val authorizationService = new AuthorizationService
  private[this] val authorizationApi = new AuthorizationApi(authorizationService)

  val routes = {
    //authenticationApi.route ~
    authorizationApi.route
  }
}
