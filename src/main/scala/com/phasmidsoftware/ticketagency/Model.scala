package com.phasmidsoftware.ticketagency

case class Seat(row: String, seat: Int)

case class Ticket(seat: Seat, price: Int)

case class Payment(value: Int)


