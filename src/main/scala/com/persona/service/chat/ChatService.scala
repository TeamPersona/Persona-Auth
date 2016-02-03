package com.persona.service.chat

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.stream.Materializer

class ChatService(implicit actorSystem: ActorSystem, materializer: Materializer) {

  def chat(offerId: Int, user: String) = {
    ChatRooms.find(offerId) match {
      case Some(room) => handleWebsocketMessages(room.websocketFlow(user))
      case None => complete(StatusCodes.NotFound)
    }
  }

  def createRoom(offerId: Int): Unit = {
    ChatRooms.createRoom(offerId)
  }

}

object ChatService {

  def apply()(implicit actorSystem: ActorSystem, materializer: Materializer) = {
    new ChatService
  }

}