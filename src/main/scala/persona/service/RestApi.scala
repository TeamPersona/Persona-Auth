package persona.service

import persona.service.authentication.facebook.FacebookAuthService
import persona.service.authentication.google.GoogleAuthService
import persona.service.authentication.{PersonaAuthService, AuthenticationApi}

trait RestApi {
  private[this] val personaAuthService = new PersonaAuthService
  private[this] val facebookAuthService = new FacebookAuthService
  private[this] val googleAuthService = new GoogleAuthService
  private[this] val authenticationApi = new AuthenticationApi(personaAuthService,
                                                              facebookAuthService,
                                                              googleAuthService)

  val routes = {
    authenticationApi.route
  }
}
