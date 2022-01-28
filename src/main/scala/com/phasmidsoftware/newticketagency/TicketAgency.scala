package com.phasmidsoftware.newticketagency

import akka.actor.typed.{ActorRef, ActorSystem}

object TicketAgency extends App {

  val rows = Map("A" -> 100, "B" -> 75, "C" -> 50)
  val tickets: Set[Ticket] = (for ((r, p) <- rows) yield for (x <- 1 until 10) yield Ticket(Seat(r, x), p)).flatten.toSet

  val system = ActorSystem(Agency(), "TicketAgency")

  val ticketAgency: ActorRef[Request] = system

  ticketAgency ! new TicketSet(tickets, ticketAgency)
//  ticketAgency ! CreateTicketPool(tickets, ticketAgency)

//  ticketAgency ! SeatRequest(2, 100)

}
