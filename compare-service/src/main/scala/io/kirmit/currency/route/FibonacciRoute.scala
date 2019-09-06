package io.kirmit.currency.route

import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.kirmit.currency.service.FibonacciSequence

class FibonacciRoute(fibonacciSequence: FibonacciSequence) {

  val route: Route = path("fib") {
    get {
      parameter('idx.as[Int]) { idx =>
        complete(fibonacciSequence.row(idx).toString())
      }
    }
  }
}
