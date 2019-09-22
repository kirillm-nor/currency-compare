package io.kirmit.currency.app

import akka.actor._
import akka.actor.typed.scaladsl
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.ActorMaterializer
import io.kirmit.currency.route.FibonacciRoute
import io.kirmit.currency.service.FibonacciSequence

import scala.concurrent.ExecutionContext
import scala.language.postfixOps

object CurrencyCompareBindApp extends App with AppSetup {

  override def httpBinding(context: scaladsl.ActorContext[_])(
      implicit system: ActorSystem,
      materializer: ActorMaterializer,
      executor: ExecutionContext,
      logging: LoggingAdapter
  ) = {
    val (host, port)   = "127.0.0.1" -> 8080
    val fibonacciRoute = new FibonacciRoute(FibonacciSequence())
    val settings       = ServerSettings(system).withMaxConnections(1024)
    Http().bindAndHandle(fibonacciRoute.route, host, port, settings = settings)
  }
}
