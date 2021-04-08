package com.phasmidsoftware.ticketagency

import akka.actor.typed._

trait Request

case class CreateTicketPool(ts: Set[Ticket]) extends Request

case class Seller(ticketSeller: ActorRef[CompletedTransaction])

case class Status(replyTo: ActorRef[Initialize])

case class Initialize(replyTo: ActorRef[Initialize])

trait Transaction

case class CompletedTransaction(ts: Set[Ticket], payment: Payment) extends Transaction

case class ProformaTransaction(quantity: Int, price: Int, replyTo: ActorRef[Transaction]) extends Transaction

case class TicketBlock(ts: Set[Ticket]) extends Transaction

case class SeatRequest(quantity: Int, price: Int) extends Request

case class Seats(ts: Set[Ticket]) extends Request


