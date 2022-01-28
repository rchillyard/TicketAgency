package com.phasmidsoftware.newticketagency

import akka.actor.typed._

case class Response(result: String)

trait Request {
  def replyTo: ActorRef[Response]
}

case class Transaction(ts: Set[Ticket], payment: Payment, replyTo: ActorRef[Response]) extends Request

case class TicketSet(ts: Set[Ticket], replyTo: ActorRef[Response]) extends Request




