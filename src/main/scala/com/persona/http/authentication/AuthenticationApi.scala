package com.persona.http.authentication

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import com.nimbusds.jwt.JWT
import com.persona.service.authentication.facebook.FacebookAuthService
import com.persona.service.authentication.google.GoogleAuthService
import com.persona.service.authentication.{PersonaAuthService, BasicAuthJsonProtocol, BasicAuth}

class AuthenticationApi(
  personaAuthService: PersonaAuthService,
  facebookAuthService: FacebookAuthService,
  googleAuthService: GoogleAuthService) extends SprayJsonSupport
                                        with BasicAuthJsonProtocol {

  val route = {
    pathPrefix("auth" / "v1") {
      pathEndOrSingleSlash {
        post {
          formFields("id", "password").as(BasicAuth) { basicAuth =>
            complete(personaAuthService.authenticate(basicAuth))
          }
        }
      } ~
      path("facebook") {
        pathEndOrSingleSlash {
          post {
            complete(facebookAuthService.authenticate)
          }
        }
      } ~
      path("google") {
        pathEndOrSingleSlash {
          post {
            complete("Test")
            /*
            formFields("id_token").as[JWT] { idToken =>
              complete(googleAuthService.authenticate(idToken))
            }
            */
          }
        }
      }
    }
  }

}
