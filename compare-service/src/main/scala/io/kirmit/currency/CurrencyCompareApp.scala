package io.kirmit.currency

import java.util.concurrent.atomic.AtomicReference

import akka.actor._
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.{IncomingConnection, ServerBinding}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl._
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.language.postfixOps

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
    implicit val logging: LoggingAdapter         = Logging(system, classOf[CurrencyCompareApp.type])

    Behaviors.receiveMessage { _ =>
      val (host, port)                                                    = "localhost" -> 8080
      val serverSource: Source[IncomingConnection, Future[ServerBinding]] = Http().bind(host, port)
      val terminationWatcher = Flow[IncomingConnection].watchTermination() { (_, terminated) =>
        terminated.failed.foreach(ex => logging.error(ex, "Connection "))
      }
      val binding: Future[ServerBinding] = serverSource
        .via(terminationWatcher)
        .mapAsyncUnordered(100) { incomming =>
          logging.debug("Established connection {}", incomming.remoteAddress)
          Future(Unit)
        }
        .to(Sink.ignore)
        .run() //.mapAsyncUnordered(100)
      binding.failed.foreach { ex =>
        logging.error(ex, "Failed to bind to {}:{}!", host, port)
      }
      Behaviors.same
    }
  }

}
