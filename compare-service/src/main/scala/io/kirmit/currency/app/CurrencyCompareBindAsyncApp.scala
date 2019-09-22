package io.kirmit.currency.app

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.settings.ServerSettings
import akka.stream.ActorMaterializer
import io.kirmit.currency.route.FibonacciRoute
import io.kirmit.currency.service.FibonacciSequence

import scala.concurrent.ExecutionContext

object CurrencyCompareBindAsyncApp extends App with AppSetup {
  override def httpBinding(context: ActorContext[_])(
      implicit system: ActorSystem,
      materializer: ActorMaterializer,
      executor: ExecutionContext,
      logging: LoggingAdapter
  ) = {
    val (host, port)   = "127.0.0.1" -> 8080
    val fibonacciRoute = new FibonacciRoute(FibonacciSequence())
    val settings       = ServerSettings(system).withMaxConnections(1024)
    Http().bindAndHandleAsync(Route.asyncHandler(fibonacciRoute.route), host, port, settings = settings, parallelism = 1024)
  }
}
