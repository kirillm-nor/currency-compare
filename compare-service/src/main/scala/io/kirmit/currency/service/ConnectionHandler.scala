package io.kirmit.currency.service

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http.IncomingConnection
import akka.http.scaladsl.server.Route
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep}
import io.kirmit.currency.monitor.ConnectionMonitor
import io.kirmit.currency.monitor.ConnectionMonitor.{ConnectionClosed, ConnectionOpened}
import io.kirmit.currency.service.ConnectionHandler.{ClosedConnectionEvent, ConnectionEvent, FailedConnectionEvent, OpenConnectionEvent}

class ConnectionHandler(requestService: RequestActorService)(implicit materializer: Materializer) {

  val connectionBehaviour: Behavior[ConnectionEvent] = Behaviors.setup { ctx =>
    import ctx.executionContext

    val connectionMonitor = ctx.spawn(ConnectionMonitor().behavior, "connection-monitor")

    Behaviors.receiveMessage {
      case OpenConnectionEvent(conn) =>
        connectionMonitor ! ConnectionOpened(conn.remoteAddress)

        conn.flow
          .mapMaterializedValue(_ => conn.remoteAddress)
          .watchTermination() { (address, terminated) =>
            terminated.failed.foreach(ex => connectionMonitor ! ConnectionClosed(address, ex))
            (address, terminated)
          }
          .joinMat(requestService.spinnedRequestFlow(conn.remoteAddress))(Keep.left)
          .mapMaterializedValue { case (_, f) => f }
          .run()
        Behavior.same
      case FailedConnectionEvent(ex) =>
        ctx.log.error(ex, "Connection exceptionally closed")
        Behavior.stopped
      case ClosedConnectionEvent =>
        ctx.log.debug("Connection closed")
        Behavior.stopped
    }
  }
}

object ConnectionHandler {
  sealed trait ConnectionEvent

  case class OpenConnectionEvent(conn: IncomingConnection) extends ConnectionEvent
  case class FailedConnectionEvent(ex: Throwable)          extends ConnectionEvent
  case object ClosedConnectionEvent                        extends ConnectionEvent
}
