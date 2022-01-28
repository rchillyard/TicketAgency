package com.phasmidsoftware.newticketagency

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

class TicketAgencySpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "An Agency" must {
    "reply to SeatRequest" in {
      val replyProbe = createTestProbe[Response]()
      val underTest = spawn(Agency())
      val rows = Map("A" -> 100, "B" -> 75, "C" -> 50)
      val tickets: Set[Ticket] = (for ((r, p) <- rows) yield for (x <- 1 until 10) yield Ticket(Seat(r, x), p)).flatten.toSet
//      underTest ! TicketSet(tickets, replyProbe)
//      replyProbe.expectMessage(Seats(Set(Ticket(Seat("A", 1), 100), Ticket(Seat("A", 2), 100))))
    }
  }


}
