package io.kirmit.currency.service

import scala.annotation.tailrec

class FibonacciSequence {

  def row(idx: Int): BigInt = {
    @tailrec
    def row_idx(n: Int, prev: BigInt, acc: BigInt): BigInt =
      n match {
        case 0 => acc
        case x => row_idx(x - 1, acc, acc + prev)
      }
    row_idx(idx, 1, 0)
  }

}

object FibonacciSequence {
  def apply(): FibonacciSequence = new FibonacciSequence()
}
