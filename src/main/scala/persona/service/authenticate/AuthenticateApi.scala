package persona.service.authenticate

import akka.http.scaladsl.server.Directives._

class AuthenticateApi {
  private[this] val authenticateService = new AuthenticateService

  val route = pathEndOrSingleSlash {
    get {
      complete(authenticateService.authenticate)
    }
  }
}
