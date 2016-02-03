package com.persona.service.chat

import akka.actor.{Props, ActorSystem}
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.{FlowShape, OverflowStrategy}
import akka.stream.scaladsl._

class ChatRoom(id: Int, actorSystem: ActorSystem) {

  private[this] val chatActor = actorSystem.actorOf(Props(classOf[ChatRoomActor], id))

  def websocketFlow(user: String) = {
    Flow.fromGraph(
      GraphDSL.create(Source.actorRef[ChatMessage](bufferSize = 5, OverflowStrategy.fail)) {
        implicit builder => {
          chatSource => {
            import GraphDSL.Implicits._

            val fromSocket = builder.add(
              Flow[Message].collect {
                case TextMessage.Strict(txt) => ChatMessage(user, txt)
              }
            )

            val toSocket = builder.add(
              Flow[ChatMessage].map {
                case ChatMessage(sender, txt) => TextMessage(s"[sender]: $txt")
              }
            )

            val merge = builder.add(Merge[ChatEvent](2))

            val chatActorSink = Sink.actorRef[ChatEvent](chatActor, Disconnect(user))

            val actorSource = builder.materializedValue.map(ref => Connect(user, ref))

            fromSocket ~> merge.in(0)
            actorSource ~> merge.in(1)

            merge ~> chatActorSink
            chatSource ~> toSocket

            FlowShape(fromSocket.in, toSocket.out)
          }
        }
      }
    )
  }

}

object ChatRooms {

  var chatRooms = Map.empty[Int, ChatRoom]

  def find(id: Int)(implicit actorSystem: ActorSystem): Option[ChatRoom] = chatRooms.get(id)

  def createRoom(id: Int)(implicit actorSystem: ActorSystem): Unit = {
    val room = new ChatRoom(id, actorSystem)
    chatRooms += id -> room
  }

}