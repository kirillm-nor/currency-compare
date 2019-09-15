package io.kirmit.currency.service

import akka.NotUsed
import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.ActorContext
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Flow, Keep, Sink}
import akka.stream.typed.scaladsl.{ActorFlow, ActorSink, ActorSource}
import akka.stream.{Materializer, OverflowStrategy}
import io.kirmit.currency.service.RequestActorService.{CurrencyRequest, CurrencyRequestClosed, CurrencyRequestFailure, CurrencyResponse}

import scala.concurrent.Future

class RequestActorService(handler: HttpRequest => Future[HttpResponse], ctx: ActorContext[_])(implicit materializer: Materializer) {

  lazy val spinnedRequestFlow: Flow[HttpRequest, HttpResponse, _] = {
    val (outActor, publisher) = ActorSource
      .actorRef[CurrencyResponse](
        completionMatcher = {
          case RequestActorService.CurrencyConnectionClosed =>
        },
        failureMatcher = {
          case RequestActorService.CurrencyResponseFailure(ex) => ex
        },
        64,
        OverflowStrategy.backpressure
      )
      .collect({ case RequestActorService.CurrencyResponseDone(response) => response })
      .toMat(Sink.asPublisher(false))(Keep.both)
      .run()

    val refSink =
      ActorSink.actorRef(ctx.spawn(provisionBehaviour, "provisionActor"), CurrencyRequestClosed, ex => CurrencyRequestFailure(ex))

    /*Flow.fromSinkAndSource(
      ActorSink.actorRef(
        factory.actorOf(Props(new Actor {
          val flowActor = context.watch(context.actorOf(props(outActor), "flowActor"))

          def receive = {
            case Status.Success(_) | Status.Failure(_) => flowActor ! PoisonPill
            case Terminated(_)                         => context.stop(self)
            case other                                 => flowActor ! other
          }

          override def supervisorStrategy = OneForOneStrategy() {
            case _ => SupervisorStrategy.Stop
          }
        })),
        Status.Success(())
      ),
      Source.fromPublisher(publisher)
    )*/
  }

  private[this] def provisionBehaviour: Behavior[CurrencyRequest] = ???
}

object RequestActorService {

  sealed trait CurrencyRequest
  case object CurrencyRequestClosed                extends CurrencyRequest
  case class CurrencyRequestFailure(ex: Throwable) extends CurrencyRequest
  sealed trait CurrencyResponse
  case object CurrencyConnectionClosed                    extends CurrencyResponse
  case class CurrencyResponseDone(response: HttpResponse) extends CurrencyResponse
  case class CurrencyResponseFailure(ex: Throwable)       extends CurrencyResponse

}
