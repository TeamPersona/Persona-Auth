package com.persona.service.chat

import akka.actor.{ActorRef, Actor}

class ChatRoomActor(offerId: Int) extends Actor {

  var participants = Map.empty[String, ActorRef]
  var supports = Map.empty[String, ActorRef]

  override def receive = {
    case Connect(user, ref) =>
      userType(user) match {
        case UserType.Consumer =>
          participants += user -> ref
          supports.values.foreach(_ ! ChatMessage("System", user + " Joined"))

        case UserType.Partner =>
          supports += user -> ref
      }
      println(user + " Joined")

    case Disconnect(user) =>
      userType(user) match {
        case UserType.Consumer =>
          participants -= user
        case UserType.Partner =>
          supports -= user
      }
      println(user + " Left")

    case msg: ChatMessage =>
      if(userType(msg.user) == UserType.Partner) {
          participants.values.foreach(_ ! msg)
      }
      supports.values.foreach(_ ! msg)
  }

  def userType(user: String) = {
    user match {
      case "support" => UserType.Partner
      case _ => UserType.Consumer
    }
  }
}
