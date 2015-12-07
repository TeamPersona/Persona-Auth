package persona.service

import akka.http.scaladsl.server.Directives._
import persona.service.authentication.facebook.FacebookAuthService
import persona.service.authentication.google.GoogleAuthService
import persona.service.authentication.{PersonaAuthService, AuthenticationApi}
import persona.service.token.{TokenApi, TokenService}

trait RestApi {
  private[this] val personaAuthService = new PersonaAuthService
  private[this] val facebookAuthService = new FacebookAuthService
  private[this] val googleAuthService = new GoogleAuthService
  private[this] val authenticationApi = new AuthenticationApi(personaAuthService,
                                                              facebookAuthService,
                                                              googleAuthService)

  private[this] val tokenService = new TokenService
  private[this] val tokenApi = new TokenApi(tokenService)

  val routes = {
    authenticationApi.route ~
    tokenApi.route
  }
}
