package com.phasmidsoftware.newticketagency

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

object Agency {

  def apply(): Behavior[Request] = createAgency(State.empty)

  private def createAgency(state: State): Behavior[Request] =
    Behaviors.receive { (context, message) =>
      message match {
        case Transaction(ts, p, replyTo) =>
          val behavior = createAgency(state.update(ts, p))
          replyTo ! Response("Transaction processed")
          behavior

        case TicketSet(ts, replyTo) =>
          val behavior = createAgency(State(ts, Nil))
          replyTo ! Response("Tickets set")
          behavior

        case m => throw TicketAgencyException(s"unexpected message type: $m")
      }
    }
}

case class TicketAgencyException(str: String) extends Exception(str)
