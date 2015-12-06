package persona.service.authentication

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.server.Directives._
import persona.service.authentication.facebook.FacebookAuthService
import persona.service.authentication.google.GoogleAuthService
import spray.json.DefaultJsonProtocol._

class AuthenticationApi(
  personaAuthService: PersonaAuthService,
  facebookAuthService: FacebookAuthService,
  googleAuthService: GoogleAuthService) extends SprayJsonSupport {

  implicit val basicAuthJsonParser = jsonFormat2(BasicAuth)

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
            complete(googleAuthService.authenticate)
          }
        }
      }
    }
  }

}
