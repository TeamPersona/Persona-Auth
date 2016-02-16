package com.persona.http.bank

import java.util.UUID

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._

import com.persona.service.account.Account
import com.persona.service.bank.{DataItem, BankService, DataItemJsonProtocol}
import com.persona.util.PersonaError

import scala.concurrent.ExecutionContext
import scala.util.Success

import spray.json._

import scalaz.NonEmptyList

class BankApi(bankService: BankService)(implicit ec: ExecutionContext)
  extends SprayJsonSupport
    with DataItemJsonProtocol {

  val route = {
    pathPrefix("bank") {
      pathEndOrSingleSlash {
        post {
          entity(as[DataItem]) { item =>
            val test_uuid = UUID.fromString("da73919b-3650-4cc7-be06-b74ef16c4b3a")
            val test_account = new Account(test_uuid)

            onComplete(bankService.saveInformation(test_account, item)) {
              case Success(result) =>
                result.fold(parseErrors => {
                  complete(StatusCodes.BadRequest, generateErrorJson(parseErrors))
                }, _ => {
                  complete(StatusCodes.OK)
                })

              case _ => complete(StatusCodes.InternalServerError)
            }
          }
        } ~
        get {
          val test_uuid = UUID.fromString("da73919b-3650-4cc7-be06-b74ef16c4b3a")
          val test_account = new Account(test_uuid)

          onComplete(bankService.listInformation(test_account)) {
            case Success(dataItems) => complete(dataItems.toJson)
            case _ => complete(StatusCodes.InternalServerError)
          }
        }
      }
    }
  }

  def generateErrorJson[T <: PersonaError](errors: NonEmptyList[T]): JsValue = {
    val errorMessages = errors.list.map { error =>
      JsString(error.errorMessage)
    }

    JsArray(errorMessages.toVector)
  }
}
