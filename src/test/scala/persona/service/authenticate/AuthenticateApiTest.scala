package persona.service.authenticate

import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}
import persona.service.RestApi

class AuthenticateApiTest extends WordSpec with Matchers with ScalatestRouteTest with RestApi {
  "Authenticate api" should {
    "return hello world when authenticating" in {
      Get("/v1") ~> routes ~> check {
        response.status should be(OK)
        responseAs[String] should be("Hello, world!")
      }
    }
  }
}
