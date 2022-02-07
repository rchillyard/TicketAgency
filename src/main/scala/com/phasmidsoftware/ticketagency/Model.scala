package com.phasmidsoftware.ticketagency

case class Seat(row: String, seat: Int)

case class Ticket(seat: Seat, price: Int)

case class Payment(value: Int)

object Payment {
  implicit object z extends Numeric[Payment] {
    def plus(x: Payment, y: Payment): Payment = Payment(x.value + y.value)

    def minus(x: Payment, y: Payment): Payment = ???

    def times(x: Payment, y: Payment): Payment = ???

    def negate(x: Payment): Payment = ???

    def fromInt(x: Int): Payment = ???

    def parseString(str: String): Option[Payment] = ???

    def toInt(x: Payment): Int = x.value

    def toLong(x: Payment): Long = ???

    def toFloat(x: Payment): Float = ???

    def toDouble(x: Payment): Double = ???

    def compare(x: Payment, y: Payment): Int = x.value.compareTo(y.value)
  }
}


