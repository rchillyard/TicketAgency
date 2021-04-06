package com.phasmidsoftware.ticketagency

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors

import scala.collection.immutable.ListSet

object Agency {

  def apply(): Behavior[AdminAction] =
    Behaviors.setup { context =>

      Behaviors.receiveMessage { message =>
        message match {
          case CreateTicketPool(ts) => context.spawn(TicketSeller(ts, Nil), "ticket seller")
        }


        val agent = context.spawn(Agent(), "agent")
        //#create-actors
        //        val replyTo: ActorRef[Agent.Greeted] = context.spawn(GreeterBot(max = 3), message.name)
        //#create-actors
        //        agent ! Agent.Greet(message.name, replyTo)
        Behaviors.same
      }
    }

}

object TicketSeller {
  def apply(tickets: Set[Ticket], payments: List[Payment]): Behavior[Transaction] = processTransactions(tickets, payments)

  private def processTransactions(tickets: Set[Ticket], payments: List[Payment]): Behavior[Transaction] = {
    Behaviors.receive {
      (context, message) =>
        val ticketsRequested = message.ss
        if ((ticketsRequested intersect tickets).size == ticketsRequested.size) {
          val ticketsRemaining = tickets diff ticketsRequested
          if (ticketsRemaining.nonEmpty)
            processTransactions(ticketsRemaining, message.payment :: payments)
          else
            Behaviors.stopped
        }
        else
          throw new Exception("requested tickets are not available")
    }
  }
}

object Agent {
  def apply(): Behavior[Int] = ???
}