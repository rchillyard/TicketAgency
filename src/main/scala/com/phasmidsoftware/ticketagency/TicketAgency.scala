package com.phasmidsoftware.ticketagency

import akka.actor.typed.ActorSystem

object TicketAgency extends App {

  val rows = Map("A" -> 100, "B" -> 75, "C" -> 50)
  val tickets: Set[Ticket] = (for ((r, p) <- rows)
    yield for (x <- 1 until 10)
      yield Ticket(Seat(r, x), p)).flatten.toSet

  val ticketAgency: ActorSystem[Request] = ActorSystem(Agency(), "TicketAgency")

  ticketAgency ! CreateTicketPool(tickets, ticketAgency.ref)
  ticketAgency ! CreateTicketPool(tickets, ticketAgency.ref)
  ticketAgency ! CreateTicketPool(tickets, ticketAgency.ref)


  ticketAgency ! SeatRequest(2, 100, ticketAgency.ref)
  ticketAgency ! SeatRequest(1, 100, ticketAgency.ref)
  ticketAgency ! SeatRequest(3, 100, ticketAgency.ref)

}
