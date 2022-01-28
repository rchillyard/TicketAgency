package com.phasmidsoftware.newticketagency

case class State(tickets: Set[Ticket], payments: List[Payment]) {
  def update(ts: Set[Ticket], payment: Payment): State = copy(tickets = tickets diff ts, payments = payment :: payments)

}

object State {
  val empty = State(Set.empty, Nil)
}
case class Seat(row: String, seat: Int)

case class Ticket(seat: Seat, price: Int)

case class Payment(value: Int)

