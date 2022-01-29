package com.phasmidsoftware.ticketagency

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef,Behavior}

import scala.concurrent.duration.DurationInt

object Agency{

  def apply(): Behavior[Request] = createAgency(List.empty)

  private def createAgency(maybePool: List[ActorRef[Transaction]]): Behavior[Request] =
    Behaviors.receive { (context, message) =>
      message match {
        case CreateTicketPool(ts, replyTo) =>
          context.log.info(s"CreateTicketPool(${ts.size}) received")
          context.log.info(s"createAgency with ticket pool no. ${maybePool.size}")
          createAgency(maybePool.appended(context.spawn(TicketPool(ts, Nil), s"ticketPool-${maybePool.size}")))
        case SeatRequest(x, p, replyTo) =>
          //case for requesting 0 seat
          if(x <= 0) {
            throw TicketAgencyException("Seats must be greater than 0")
          }
          else if(maybePool.nonEmpty) {
            implicit val timeout: akka.util.Timeout = 15.seconds
            val pool = scala.util.Random.shuffle(maybePool).head
            context.log.info(s"SeatRequest($x, $p) received with maybePool = $pool")
            pool.ref ! ProformaTransaction(x, p, replyTo)
          }
          else {
            throw TicketAgencyException("no pool of tickets is available")
          }
          Behaviors.same
        case seats: Seats =>
          context.log.info(s"Seats booked ${seats.ts}")
          Behaviors.same
      }
    }
}

object TicketSeller {
  def apply(tickets: Set[Ticket], payments: List[Payment]): Behavior[Transaction] =
    processTransactions(tickets, payments)

  private def processTransactions(tickets: Set[Ticket], payments: List[Payment]): Behavior[Transaction] =
    if (tickets.nonEmpty)
      Behaviors.receive {
        (_, message) =>
          message match {
            case CompletedTransaction(ticketsRequested, payment, replyTo) =>
              if ((ticketsRequested intersect tickets).size == ticketsRequested.size) {
                val ts = tickets diff ticketsRequested
                replyTo ! Seats(ticketsRequested)
                processTransactions(ts, payment :: payments)
              }
              else
                throw new Exception("requested tickets are not available")

            case _ => throw new Exception("unsupported Transaction type")
          }
      }
    else
      Behaviors.stopped
}

object TicketPool {
  def apply(tickets: Set[Ticket], payments: List[Payment]): Behavior[Transaction] = {
    Behaviors.setup { context =>
      val ticketSeller = context
        .spawn(TicketSeller(tickets, payments), "TicketSeller")
      processTransactions(tickets, payments, ticketSeller)
    }
  }

  private def processTransactions(tickets: Set[Ticket],
                                  payments: List[Payment],
                                  ticketSeller: ActorRef[Transaction]): Behavior[Transaction] = {
    Behaviors.receive {
      (_, message) =>
        message match {
          case ProformaTransaction(quantity, price, replyTo) =>
            val ts: Set[Ticket] = tickets filter (t => t.price == price) take quantity
            ticketSeller ! CompletedTransaction(ts, Payment(price), replyTo)
            if (ts.nonEmpty)
              processTransactions(ts, payments, ticketSeller)
            else
              Behaviors.stopped
        }
    }
  }
}

case class TicketAgencyException(str: String) extends Exception(str)