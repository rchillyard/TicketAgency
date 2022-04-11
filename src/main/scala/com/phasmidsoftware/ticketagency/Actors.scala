package com.phasmidsoftware.ticketagency

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}

import scala.concurrent.duration.DurationInt

object Agency {

  def apply(): Behavior[Request] = create(List.empty)

  private def create(transactions: List[ActorRef[Transaction]]): Behavior[Request] =
    Behaviors.receive { (context, message) =>
      message match {
        case CreateTicketPool(ts, _) =>
          context.log.info(s"CreateTicketPool(${ts.size}) received")
          context.log.info(s"create with ticket transaction no. ${transactions.size}")
          create(transactions.appended(context.spawn(TicketPool(ts, Nil), s"ticketPool-${transactions.size}")))
        case SeatRequest(x, p, replyTo) =>
          if (x > 0)
            if (transactions.nonEmpty) {
              implicit val timeout: akka.util.Timeout = 15.seconds
              val transaction = scala.util.Random.shuffle(transactions).head
              context.log.info(s"SeatRequest($x, $p) received with transaction = $transaction")
              transaction.ref ! ProformaTransaction(x, p, replyTo)
              Behaviors.same
            }
            else throw TicketAgencyException("no tickets available")
          else throw TicketAgencyException("seats requested must be greater positive")
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
    else {
      val z = implicitly[Counting[Payment]]
      System.err.println(s"All tickets sold for ${payments.foldLeft(z.zero)((a,x) => z.plus(a,x))}")
      Behaviors.stopped
    }
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
      (context, message) =>
        message match {
          case ProformaTransaction(quantity, price, replyTo) =>
            context.log.info(s"processTransactions: tickets: $tickets")
            val ts: Set[Ticket] = tickets filter (t => t.price == price) take quantity
            val remaining: Set[Ticket] = tickets diff ts
            val payment = Payment(price * quantity)
            ticketSeller ! CompletedTransaction(ts, payment, replyTo)
            context.log.info(s"processTransactions: ts: $ts")
            if (ts.nonEmpty)
              processTransactions(remaining, payments :+ payment, ticketSeller)
            else
              Behaviors.stopped
        }
    }
  }
}

case class TicketAgencyException(str: String) extends Exception(str)