package com.phasmidsoftware.ticketagency

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class TicketAgencySpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "An Agency" must {
    "reply to SeatRequest" in {
      val replyProbe = createTestProbe[Request]()
      val underTest = spawn(Agency())
      val rows = Map("A" -> 100, "B" -> 75, "C" -> 50)
      val tickets: Set[Ticket] = (for ((r, p) <- rows) yield for (x <- 1 until 10) yield Ticket(Seat(r, x), p)).flatten.toSet
      underTest ! CreateTicketPool(tickets)
      underTest ! SeatRequest(2, 100)
      replyProbe.expectMessage(Seats(Set(Ticket(Seat("A", 8), 100), Ticket(Seat("A", 6), 100))))
    }
  }


}
