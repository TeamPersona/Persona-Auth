package persona.service.token

import akka.actor.{ActorRef, PoisonPill}

class ActorWrapper(protected val actor: ActorRef) {

  def stop(): Unit = actor ! PoisonPill

}
