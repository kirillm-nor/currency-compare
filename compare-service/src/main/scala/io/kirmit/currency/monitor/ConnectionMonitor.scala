package io.kirmit.currency.monitor

import java.net.InetSocketAddress

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.kirmit.currency.monitor.ConnectionMonitor.{ConnectionClosed, ConnectionOpened, MonitorMessage}

final class ConnectionMonitor {
  val behavior: Behavior[MonitorMessage] = connections(List[InetSocketAddress]())

  private[this] def connections(conns: List[InetSocketAddress]): Behavior[MonitorMessage] =
    Behaviors.receive { (context, msg) =>
      msg match {
        case ConnectionOpened(address) =>
          connections(address :: conns)
        case ConnectionClosed(address) =>
          connections(conns.filterNot(_ == address))
      }
    }
}

object ConnectionMonitor {

  sealed trait MonitorMessage
  final case class ConnectionOpened(address: InetSocketAddress) extends MonitorMessage
  final case class ConnectionClosed(address: InetSocketAddress) extends MonitorMessage

  def apply: ConnectionMonitor = new ConnectionMonitor()
}
