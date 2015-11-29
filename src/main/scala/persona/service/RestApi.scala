package persona.service

import akka.http.scaladsl.server.Directives._
import persona.service.authenticate.AuthenticateApi

trait RestApi {
  private[this] val authenticateApi = new AuthenticateApi

  val routes = pathPrefix("v1") {
    authenticateApi.route
  }
}
