package com.persona.service.chat

import akka.actor.ActorRef

sealed trait ChatEvent
case class Connect(user: String, ref: ActorRef) extends ChatEvent
case class Disconnect(user: String) extends ChatEvent
case class GetLog(user: String) extends ChatEvent
case class ChatLog(log: List[String]) extends ChatEvent
case class ChatMessage(user: String, msg: String) extends ChatEvent

