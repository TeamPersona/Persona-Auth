package persona.service.authentication

import akka.http.scaladsl.model.FormData
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import org.scalatest.{Matchers, WordSpec}
import persona.service.RestApi

class AuthenticationApiTest extends WordSpec with Matchers with ScalatestRouteTest with RestApi {

  "Authenticate api" should {
    "echo back id when authenticating" in {
      val testID = "TestID"
      val formData = FormData("id" -> testID, "password" -> "abc123")

      Post("/auth/v1", formData) ~> routes ~> check {
        response.status should be(OK)
        responseAs[String] should be(s"Hello, $testID!")
      }
    }

    "allow users to login with facebook" in {
      Post("/auth/v1/facebook") ~> routes ~> check {
        response.status should be(OK)
        responseAs[String] should be("Logging in to Facebook")
      }
    }

    "allow users to login with google" in {
      Post("/auth/v1/google") ~> routes ~> check {
        response.status should be(OK)
        responseAs[String] should be("Logging in to Google")
      }
    }
  }

}
