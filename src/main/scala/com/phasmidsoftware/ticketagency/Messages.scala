package com.phasmidsoftware.ticketagency

import akka.actor.typed._

trait Request

//implement replyTo for childActor to send message to main Agency Actor
case class CreateTicketPool(ts: Set[Ticket], replyTo: ActorRef[Request]) extends Request

case class Seller(ticketSeller: ActorRef[CompletedTransaction])

case class Status(replyTo: ActorRef[Initialize])

case class Initialize(replyTo: ActorRef[Initialize])

trait Transaction

//implement replyTo for childActor to send message to main Agency Actor
case class CompletedTransaction(ts: Set[Ticket], payment: Payment, replyTo: ActorRef[Request])
  extends Transaction

case class ProformaTransaction(quantity: Int, price: Int, replyTo: ActorRef[Request]) extends Transaction

case class TicketBlock(ts: Set[Ticket]) extends Transaction

case class SeatRequest(quantity: Int, price: Int, replyTo: ActorRef[Request]) extends Request

case class Seats(ts: Set[Ticket]) extends Request


