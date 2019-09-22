package io.kirmit.currency.app

import akka.actor._
import akka.actor.typed.scaladsl.ActorContext
import akka.event.LoggingAdapter
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.{IncomingConnection, ServerBinding}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.stream.typed.scaladsl.ActorSink
import io.kirmit.currency.monitor.ConnectionMonitor
import io.kirmit.currency.monitor.ConnectionMonitor.{ConnectionClosed, ConnectionOpened}
import io.kirmit.currency.route.FibonacciRoute
import io.kirmit.currency.service.{FibonacciSequence, RequestActorService}

import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps

object CurrencyCompareApp extends App with AppSetup {

  def httpBinding(context: ActorContext[_])(implicit system: ActorSystem,
                                            materializer: ActorMaterializer,
                                            executor: ExecutionContext,
                                            logging: LoggingAdapter): Future[ServerBinding] = {
    val (host, port)                                                    = "127.0.0.1" -> 8080
    val fibonacciRoute                                                  = new FibonacciRoute(FibonacciSequence())
    val serverSource: Source[IncomingConnection, Future[ServerBinding]] = Http().bind(host, port)
    val terminationWatcher = Flow[IncomingConnection].watchTermination() { (_, terminated) =>
      terminated.failed.foreach(ex => logging.error(ex, "Server is down"))
    }
    val connectionMonitor   = context.spawn(ConnectionMonitor().behavior, "connection-monitor")
    val requestActorService = new RequestActorService(Route.asyncHandler(fibonacciRoute.route), context)

    serverSource
      .via(terminationWatcher)
      .mapAsyncUnordered(1024) { incoming =>
        connectionMonitor ! ConnectionOpened(incoming.remoteAddress)

        incoming.flow
          .mapMaterializedValue(_ => incoming.remoteAddress)
          .watchTermination() { (address, terminated) =>
            terminated.failed.foreach(ex => connectionMonitor ! ConnectionClosed(address, ex))
            (address, terminated)
          }
          .joinMat(requestActorService.spinnedRequestFlow(incoming.remoteAddress))(Keep.left)
          .mapMaterializedValue { case (_, f) => f }
          .run()
      }
      .to(Sink.ignore)
      .run()
  }
}
