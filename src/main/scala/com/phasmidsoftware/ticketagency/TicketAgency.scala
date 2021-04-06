package com.phasmidsoftware.ticketagency

import akka.actor.typed.ActorSystem

object TicketAgency extends App {

  val ticketAgency: ActorSystem[AdminAction] = ActorSystem(Agency(), "TicketAgency")

}
