package com.phasmidsoftware.ticketagency

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object Agency {

  def apply(): Behavior[Request] = createAgency(None)

  private def createAgency(maybePool: Option[ActorRef[Transaction]]): Behavior[Request] =
    Behaviors.setup { context =>
      context.log.info(s"createAgency with maybePool = $maybePool")

      Behaviors.receiveMessage { message =>
        message match {
          case CreateTicketPool(ts) =>
            context.log.info(s"CreateTicketPool(${ts.size}) received")
            maybePool match {
              case None =>
                context.log.info(s"createAgency with ticket pool: None")
                createAgency(Some(context.spawn(TicketPool(ts, Nil), "ticketPool")))
              case Some(_) => throw TicketAgencyException("pool already set up") // TODO allow other pools
            }
          case SeatRequest(x, p) =>
            context.log.info(s"SeatRequest($x, $p) received with maybePool = $maybePool")
            maybePool match {
              case Some(pool) =>
                implicit val timeout: akka.util.Timeout = 3.seconds
                context.ask(pool, ref => ProformaTransaction(x, p, ref)) {
                  case Success(TicketBlock(ts)) => Seats(ts)
                  case Success(x) => throw TicketAgencyException(s"wrong response: $x")
                  case Failure(x) => throw TicketAgencyException(s"failure: $x")
                }

              case None => throw TicketAgencyException("no pool of tickets is available")
            }
        }


//        val agent = context.spawn(Agent(), "agent")
        //#create-actors
        //        val replyTo: ActorRef[Agent.Greeted] = context.spawn(GreeterBot(max = 3), message.name)
        //#create-actors
        //        agent ! Agent.Greet(message.name, replyTo)
        Behaviors.same
      }
    }

}

object TicketSeller {
  def apply(tickets: Set[Ticket], payments: List[Payment]): Behavior[Transaction] = processTransactions(tickets, payments)

  private def processTransactions(tickets: Set[Ticket], payments: List[Payment]): Behavior[Transaction] =
    if (tickets.nonEmpty)
      Behaviors.receive {
        (_, message) =>
          message match {
            case CompletedTransaction(ticketsRequested, payment) =>
              if ((ticketsRequested intersect tickets).size == ticketsRequested.size)
                processTransactions(tickets diff ticketsRequested, payment :: payments)
              else
                throw new Exception("requested tickets are not available")

            case _ => throw new Exception("unsupported Transaction type")
          }
      }
    else
      Behaviors.stopped
}

object TicketPool {
  def apply(tickets: Set[Ticket], payments: List[Payment]): Behavior[Transaction] = processTransactions(tickets, payments)

  private def processTransactions(tickets: Set[Ticket], payments: List[Payment]): Behavior[Transaction] = {
    Behaviors.receive {
      (context, message) =>
        message match {
          case CompletedTransaction(ticketsRequested, payment) =>
            if ((ticketsRequested intersect tickets).size == ticketsRequested.size)
              makeBehavior(payment :: payments, tickets diff ticketsRequested)
            else
              throw new Exception("requested tickets are not available")

          case ProformaTransaction(quantity, price, replyTo) =>
            val ts: Set[Ticket] = tickets filter (t => t.price == price) take quantity
            replyTo ! TicketBlock(ts)
            makeBehavior(payments, tickets diff ts)
        }
    }
  }

  private def makeBehavior(payments: List[Payment], ticketsRemaining: Set[Ticket]): Behavior[Transaction] = {
    if (ticketsRemaining.nonEmpty)
      processTransactions(ticketsRemaining, payments)
    else
      Behaviors.stopped
  }
}

object Agent {
  def apply(): Behavior[Int] = ???
}

case class TicketAgencyException(str: String) extends Exception(str)