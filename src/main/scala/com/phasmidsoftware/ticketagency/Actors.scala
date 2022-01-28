package com.phasmidsoftware.ticketagency

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef,Behavior}

import scala.concurrent.duration.DurationInt
import scala.util.{Failure, Success}

object Agency{

  def apply(): Behavior[Request] = createAgency(List.empty)

  private def createAgency(maybePool: List[ActorRef[Transaction]]): Behavior[Request] =
    Behaviors.receive { (context, message) =>
      message match {
        case CreateTicketPool(ts) =>
          context.log.info(s"CreateTicketPool(${ts.size}) received")
          context.log.info(s"createAgency with ticket pool no. ${maybePool.size}")
          createAgency(maybePool.appended(context.spawn(TicketPool(ts, Nil), s"ticketPool-${maybePool.size}")))
        case SeatRequest(x, p) =>
          if(maybePool.nonEmpty) {
            implicit val timeout: akka.util.Timeout = 15.seconds
            val pool = scala.util.Random.shuffle(maybePool).head
            context.log.info(s"SeatRequest($x, $p) received with maybePool = $pool")
            context.ask(pool.ref, ref => ProformaTransaction(x, p, ref)) {
              case Success(TicketBlock(ts)) => Seats(ts)
              case Success(x) => throw TicketAgencyException(s"wrong response: $x")
              case Failure(x) => throw TicketAgencyException(s"failure: $x")
            }
          }
          else {
            throw TicketAgencyException("no pool of tickets is available")
          }
          Behaviors.same
        case Seats(ts) =>
          context.log.info(s"Seats booked $ts")
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
      (_, message) =>
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

case class TicketAgencyException(str: String) extends Exception(str)