package com.phasmidsoftware.ticketagency

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import org.scalatest.wordspec.AnyWordSpecLike

/**
 * The original unit test was failing because of RequestTimeout
 * the Agency actor was calling the child actor but child actor wasn't sending the message to the Agency.
 * Inside the unit test, it was expecting the message to be received on the Agency end, but it doesn't reach to the Agency actor.
 * So using the system.actorRef, and adding to replyTo, we can then send message back to the Agency and unit test will expect the message accordingly
 */

class TicketAgencySpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "An Agency" must {
    "reply to SeatRequest" in {
      val replyProbe = createTestProbe[Request]()
      val underTest = spawn(Agency())
      val rows = Map("A" -> 100, "B" -> 75, "C" -> 50)
      val tickets: Set[Ticket] = (for ((r, p) <- rows) yield for (x <- 1 until 10) yield Ticket(Seat(r, x), p)).flatten.toSet
      underTest ! CreateTicketPool(tickets, replyProbe.ref)
      underTest ! SeatRequest(2, 100, replyProbe.ref)
      replyProbe.expectMessage(Seats(Set(Ticket(Seat("A", 8), 100), Ticket(Seat("A", 6), 100))))
    }
  }


}
