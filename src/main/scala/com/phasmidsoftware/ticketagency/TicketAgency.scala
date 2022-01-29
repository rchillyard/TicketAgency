package com.phasmidsoftware.ticketagency

import akka.actor.typed.ActorSystem

object TicketAgency extends App {

  val rows = Map("A" -> 100, "B" -> 75, "C" -> 50)
  val tickets: Set[Ticket] = (for ((r, p) <- rows) yield for (x <- 1 until 10) yield Ticket(Seat(r, x), p)).flatten.toSet

  val ticketAgency: ActorSystem[Request] = ActorSystem(Agency(), "TicketAgency")

  ticketAgency ! CreateTicketPool(tickets)
<<<<<<< Updated upstream

  ticketAgency ! SeatRequest(2, 100)
=======
//  ticketAgency ! CreateTicketPool(tickets)
//  ticketAgency ! CreateTicketPool(tickets)
//  ticketAgency ! CreateTicketPool(tickets)
//  ticketAgency ! CreateTicketPool(tickets)
//  ticketAgency ! CreateTicketPool(tickets)
//  ticketAgency ! CreateTicketPool(tickets)
//  ticketAgency ! CreateTicketPool(tickets)
//  ticketAgency ! CreateTicketPool(tickets)
//  ticketAgency ! CreateTicketPool(tickets)
//  ticketAgency ! CreateTicketPool(tickets)
//  ticketAgency ! CreateTicketPool(tickets)

  ticketAgency ! SeatRequest(2, 100)
//  ticketAgency ! SeatRequest(1, 100)
//  ticketAgency ! SeatRequest(3, 100)
//  ticketAgency ! SeatRequest(5, 100)
//  ticketAgency ! SeatRequest(1, 100)
//  ticketAgency ! SeatRequest(3, 100)
//  ticketAgency ! SeatRequest(5, 100)
//  ticketAgency ! SeatRequest(3, 100)
//  ticketAgency ! SeatRequest(6, 100)

  //  val ticketSeller: ActorSystem[Transaction] = ActorSystem(TicketSeller(tickets,List(100,100,100)))


  //add line on what to do with TicketSeller & TicketPool

>>>>>>> Stashed changes

}
