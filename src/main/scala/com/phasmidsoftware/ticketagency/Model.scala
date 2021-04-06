package com.phasmidsoftware.ticketagency

case class Seat(row: String, seat: Int)

case class Ticket(seat: Seat, price: Int)

case class Transaction(ss: Set[Ticket], payment: Payment)

case class Payment(value: Int)


