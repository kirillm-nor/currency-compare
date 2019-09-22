package io.kirmit.currency.app

import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.ActorContext
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.{IncomingConnection, ServerBinding}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Flow, Source}
import akka.stream.typed.scaladsl.ActorSink
import io.kirmit.currency.route.FibonacciRoute
import io.kirmit.currency.service.ConnectionHandler.{ClosedConnectionEvent, FailedConnectionEvent, OpenConnectionEvent}
import io.kirmit.currency.service.{ConnectionHandler, FibonacciSequence, RequestActorService}

import scala.concurrent.{ExecutionContext, Future}

object CurrencyCompareBindActorApp extends App with AppSetup {
  override def httpBinding(context: ActorContext[_])(
      implicit system: ActorSystem,
      materializer: ActorMaterializer,
      executor: ExecutionContext,
      logging: LoggingAdapter
  ) = {
    val (host, port)                                                    = "127.0.0.1" -> 8080
    val fibonacciRoute                                                  = new FibonacciRoute(FibonacciSequence())
    val serverSource: Source[IncomingConnection, Future[ServerBinding]] = Http().bind(host, port)
    val terminationWatcher = Flow[IncomingConnection].watchTermination() { (_, terminated) =>
      terminated.failed.foreach(ex => logging.error(ex, "Server is down"))
    }
    val requestActorService = new RequestActorService(Route.asyncHandler(fibonacciRoute.route), context)
    val connectionHandler   = new ConnectionHandler(requestActorService)

    serverSource
      .via(terminationWatcher)
      .to(
        ActorSink
          .actorRef(context.spawn(connectionHandler.connectionBehaviour, "connection-handler"), ClosedConnectionEvent, ex => FailedConnectionEvent(ex))
          .contramap(conn => OpenConnectionEvent(conn))
      )
      .run()
  }
}
