package io.kirmit.currency.app

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.{IncomingConnection, ServerBinding}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import io.kirmit.currency.route.FibonacciRoute
import io.kirmit.currency.service.FibonacciSequence

import scala.concurrent.{ExecutionContext, Future}

object CurrencyCompareBindConnectionApp extends App with AppSetup {
  override def httpBinding(context: ActorContext[_])(
      implicit system: ActorSystem,
      materializer: ActorMaterializer,
      executor: ExecutionContext,
      logging: LoggingAdapter
  ) = {
    val (host, port)                                                    = "127.0.0.1" -> 8080
    val fibonacciRoute                                                  = new FibonacciRoute(FibonacciSequence())
    val serverSource: Source[IncomingConnection, Future[ServerBinding]] = Http().bind(host, port)

    serverSource
      .to(Sink.foreach { incoming =>
        incoming.handleWithAsyncHandler(Route.asyncHandler(fibonacciRoute.route), 1024)
      })
      .run()
  }
}
