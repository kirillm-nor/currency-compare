package io.kirmit.currency.route

import akka.event.LoggingAdapter
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import io.kirmit.currency.service.FibonacciSequence

class FibonacciRoute(fibonacciSequence: FibonacciSequence)(implicit val logger: LoggingAdapter) {

  val route: Route = path("fib") {
    get {
      parameter('idx.as[Int]) { idx =>
        complete(fibonacciSequence.row(idx).toString())
      }
    }
  }
}
