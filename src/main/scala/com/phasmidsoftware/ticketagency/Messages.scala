package com.phasmidsoftware.ticketagency

import akka.actor.typed._

trait AdminAction

case class CreateTicketPool(ts: Set[Ticket]) extends AdminAction

case class Status(replyTo: ActorRef[Initialize])

case class Initialize(replyTo: ActorRef[Initialize])
