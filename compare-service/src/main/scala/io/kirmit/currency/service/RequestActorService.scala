package io.kirmit.currency.service

import akka.NotUsed
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.actor.typed.{ActorRef, Behavior, SupervisorStrategy, Terminated}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import akka.stream.typed.scaladsl.{ActorSink, ActorSource}
import akka.stream.{Materializer, OverflowStrategy}
import io.kirmit.currency.service.RequestActorService._
import io.kirmit.currency.service.RequestProcessorActor.RequestWithSink

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

    val provisionActor = ctx.spawn(provisionBehaviour(outActor), "provisionActor")

    ctx.watch(provisionActor)

    val refSink: Sink[HttpRequest, NotUsed] =
      ActorSink
        .actorRef(provisionActor, CurrencyRequestClosed, ex => CurrencyRequestFailure(ex))
        .contramap(r => CurrencyRequestReceived(r))

    Flow.fromSinkAndSource[HttpRequest, HttpResponse](refSink, Source.fromPublisher(publisher))

  }

  private[this] def provisionBehaviour(out: ActorRef[CurrencyResponse]): Behavior[CurrencyRequest] = Behaviors.setup { ctx =>
    import scala.concurrent.duration._

    implicit val requestProcessorActor: RequestProcessorActor = RequestProcessorActor(handler)

    Behaviors
      .receiveMessagePartial[CurrencyRequest] {
        case CurrencyRequestReceived(req) =>
          val requestActor = ctx.spawn(Behaviors
                                         .supervise(RequestProcessorActor.behavior)
                                         .onFailure(SupervisorStrategy.restart.withLimit(3, 2 minutes)),
                                       req.uri.path.tail.toString())
          ctx.watch(requestActor)
          requestActor ! RequestWithSink(req, out)
          Behavior.same
        case CurrencyRequestFailure(ex) => Behavior.stopped(() => ctx.log.error(ex, "Actor failed"))
        case CurrencyRequestClosed      => Behavior.stopped
      }
      .receiveSignal {
        case (_, t: Terminated) => Behaviors.same
      }
  }
}

object RequestActorService {

  sealed trait CurrencyRequest
  case object CurrencyRequestClosed                        extends CurrencyRequest
  case class CurrencyRequestReceived(request: HttpRequest) extends CurrencyRequest
  case class CurrencyRequestFailure(ex: Throwable)         extends CurrencyRequest
  sealed trait CurrencyResponse
  case object CurrencyConnectionClosed                    extends CurrencyResponse
  case class CurrencyResponseDone(response: HttpResponse) extends CurrencyResponse
  case class CurrencyResponseFailure(ex: Throwable)       extends CurrencyResponse

}
