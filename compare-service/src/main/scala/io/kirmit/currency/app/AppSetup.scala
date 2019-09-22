package io.kirmit.currency.app

import java.util.concurrent.atomic.AtomicReference

import akka.actor.{ActorSystem, typed}
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{Behavior, Terminated}
import akka.event.{Logging, LoggingAdapter}
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.language.postfixOps
import scala.util.{Failure, Success}

trait AppSetup {
  def httpBinding(context: ActorContext[_])(implicit system: ActorSystem,
                                            materializer: ActorMaterializer,
                                            executor: ExecutionContext,
                                            logging: LoggingAdapter): Future[ServerBinding]

  lazy val systemConfig: Config                               = ConfigFactory.load()
  lazy val system: typed.ActorSystem[Config]                  = typed.ActorSystem(setupBehavior, "currency-system", systemConfig)
  protected val serverBinding: AtomicReference[ServerBinding] = new AtomicReference[ServerBinding]()

  lazy val setupBehavior: Behavior[Config] = Behaviors.setup[Config] { context =>
    implicit val timeout: Timeout                = Timeout(30 seconds)
    implicit val system: ActorSystem             = context.system.toUntyped
    implicit val materializer: ActorMaterializer = ActorMaterializer()
    implicit val executor: ExecutionContext      = system.dispatcher
    implicit val logging: LoggingAdapter         = Logging(system, classOf[AppSetup])

    Behaviors
      .receiveMessage[Config] { _ =>
        val binding: Future[ServerBinding] = httpBinding(context)

        binding.onComplete {
          case Success(b) =>
            serverBinding.set(b)
            logging.info(s"Server online at http://${b.localAddress.getHostName}:${b.localAddress.getPort}/")
          case Failure(ex) => logging.error(ex, "Failed to bind!")
        }
        Behaviors.same
      }
      .receiveSignal {
        case (_, t: Terminated) =>
          context.log.debug(s"Terminated ${t.ref.path}")
          Behavior.same
      }

  }
  system ! systemConfig

  system.whenTerminated.onComplete {
    case Success(_)         => println("System successfully stopped")
    case Failure(exception) => println(s"Exception occurred while termination $exception")
  }(system.executionContext)

}
