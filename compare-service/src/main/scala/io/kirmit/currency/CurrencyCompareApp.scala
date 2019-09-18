package io.kirmit.currency

import java.util.concurrent.atomic.AtomicReference

import akka.actor._
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.{IncomingConnection, ServerBinding}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Route
import akka.stream.{ActorMaterializer, OverflowStrategy}
import akka.stream.scaladsl._
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import io.kirmit.currency.monitor.ConnectionMonitor
import io.kirmit.currency.monitor.ConnectionMonitor.{ConnectionClosed, ConnectionOpened}
import io.kirmit.currency.route.FibonacciRoute
import io.kirmit.currency.service.{FibonacciSequence, RequestActorService}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

trait Setup {

  def setupBehavior: Behavior[Config]

  lazy val systemConfig: Config              = ConfigFactory.load()
  lazy val system: typed.ActorSystem[Config] = typed.ActorSystem(setupBehavior, "currency-system", systemConfig)
}

object CurrencyCompareApp extends App with Setup {
  private val serverBinding: AtomicReference[ServerBinding] = new AtomicReference[ServerBinding]()

  override val setupBehavior: Behavior[Config] = Behaviors.setup[Config] { context =>
    implicit val timeout: Timeout                = Timeout(30 seconds)
    implicit val system: ActorSystem             = context.system.toUntyped
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executor: ExecutionContext      = system.dispatcher
    implicit val logging: LoggingAdapter         = Logging(system, classOf[Setup])

    Behaviors.receiveMessage { _ =>
      val (host, port)                                                    = "localhost" -> 8080
      val fibonacciRoute                                                  = new FibonacciRoute(FibonacciSequence())
      val serverSource: Source[IncomingConnection, Future[ServerBinding]] = Http().bind(host, port)
      val terminationWatcher = Flow[IncomingConnection].watchTermination() { (_, terminated) =>
        terminated.failed.foreach(ex => logging.error(ex, "Server is down"))
      }
      val connectionMonitor   = context.spawn(ConnectionMonitor().behavior, "connection-monitor")
      val requestActorService = new RequestActorService(Route.asyncHandler(fibonacciRoute.route), context)

      val binding: Future[ServerBinding] = serverSource
        .via(terminationWatcher)
        .mapAsyncUnordered(100) { incoming =>
          connectionMonitor ! ConnectionOpened(incoming.remoteAddress)

          incoming.flow
            .mapMaterializedValue(_ => incoming.remoteAddress)
            .watchTermination() { (address, terminated) =>
              terminated.failed.foreach(ex => connectionMonitor ! ConnectionClosed(address, ex))
              (address, terminated)
            }
            .joinMat(requestActorService.spinnedRequestFlow)(Keep.left)
            .mapMaterializedValue { case (_, f) => f }
            .run()
        }
        .to(Sink.ignore)
        .run()

      binding.onComplete {
        case Success(b) =>
          serverBinding.set(b)
          logging.info(s"Server online at http://${b.localAddress.getHostName}:${b.localAddress.getPort}/")
        case Failure(ex) => logging.error(ex, "Failed to bind to {}:{}!", host, port)
      }
      Behaviors.same
    }
  }
  system ! systemConfig

  system.whenTerminated.onComplete {
    case Success(_)         =>
    case Failure(exception) =>
  }(system.executionContext)
}
