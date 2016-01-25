package com.persona.http.authorization

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import com.persona.service.authorization.AuthorizationService
import spray.json.JsonParser

class AuthorizationApi(
  tokenService: AuthorizationService) extends SprayJsonSupport {

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
