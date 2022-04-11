package com.phasmidsoftware.ticketagency

import scala.math.Numeric

case class Seat(row: String, seat: Int)

case class Ticket(seat: Seat, price: Int)

case class Payment(value: Int)

object Payment {
  implicit object z extends Counting[Payment] {
    def plus(x: Payment, y: Payment): Payment = Payment(x.value + y.value)

    def minus(x: Payment, y: Payment): Payment = plus(x, negate(y))

    def negate(x: Payment): Payment = Payment(-x.value)

    def fromInt(x: Int): Payment = Payment(x)

    def parseString(str: String): Option[Payment] = str.toIntOption.map(fromInt)

    def toInt(x: Payment): Int = x.value

    def toLong(x: Payment): Long = x.value

    def toFloat(x: Payment): Float = x.value

    def toDouble(x: Payment): Double = x.value

    def compare(x: Payment, y: Payment): Int = x.value.compareTo(y.value)
  }
}

trait Counting[T] extends Ordering[T] {
  def plus(x: T, y: T): T
  def minus(x: T, y: T): T
  def negate(x: T): T
  def fromInt(x: Int): T
  def parseString(str: String): Option[T]
  def toInt(x: T): Int
  def toLong(x: T): Long
  def toFloat(x: T): Float
  def toDouble(x: T): Double

  def zero: T = fromInt(0)
  def one: T = fromInt(1)

  def abs(x: T): T = if (lt(x, zero)) negate(x) else x

  @deprecated("use `sign` method instead", since = "2.13.0") def signum(x: T): Int =
    if (lt(x, zero)) -1
    else if (gt(x, zero)) 1
    else 0
  def sign(x: T): T =
    if (lt(x, zero)) negate(one)
    else if (gt(x, zero)) one
    else zero

  class CountingOps(lhs: T) {
    def +(rhs: T): T = plus(lhs, rhs)
    def -(rhs: T): T = minus(lhs, rhs)
    def unary_- : T = negate(lhs)
    def abs: T = Counting.this.abs(lhs)
//    @deprecated("use `sign` method instead", since = "2.13.0") def signum: Int = Counting.this.signum(lhs)
    def sign: T = Counting.this.sign(lhs)
    def toInt: Int = Counting.this.toInt(lhs)
    def toLong: Long = Counting.this.toLong(lhs)
    def toFloat: Float = Counting.this.toFloat(lhs)
    def toDouble: Double = Counting.this.toDouble(lhs)
  }
  import scala.language.implicitConversions
  implicit def mkNumericOps(lhs: T): CountingOps = new CountingOps(lhs)

}

