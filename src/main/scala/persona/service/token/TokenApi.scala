package persona.service.token

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import spray.json.JsonParser

class TokenApi(
  tokenService: TokenService) extends SprayJsonSupport {

  val route = {
    pathPrefix("token" / "v1") {
      path("jwks") {
        pathEndOrSingleSlash {
          get {
            val jwks = tokenService.jwks
            complete(JsonParser(jwks.toString))
          }
        }
      }
    }
  }

}
