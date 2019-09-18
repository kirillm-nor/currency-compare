package io.kirmit.currency.service

import java.net.InetSocketAddress

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

  def spinnedRequestFlow(address: InetSocketAddress): Flow[HttpRequest, HttpResponse, _] = {
    val (outActor, publisher) = ActorSource
      .actorRef[CurrencyResponse](
        completionMatcher = {
          case RequestActorService.CurrencyConnectionClosed =>
            ctx.log.debug("Closing connection")
        },
        failureMatcher = {
          case RequestActorService.CurrencyResponseFailure(ex) =>
            ctx.log.error(ex, "Response failed")
            ex
        },
        64,
        OverflowStrategy.dropTail
      )
      .collect({
        case RequestActorService.CurrencyResponseDone(response) => response
      })
      .toMat(Sink.asPublisher(false))(Keep.both)
      .run()

    val provisionActor = ctx.spawn(provisionBehaviour(outActor), s"${address.toString.replaceAll("\\p{Punct}", "")}-connectionActor")

    ctx.watch(provisionActor)
    ctx.log.debug(s"Provisioning actor created ${provisionActor.path}")

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
          ctx.log.debug(s"Sent request to actor $req ${requestActor.path}")
          requestActor ! RequestWithSink(req, out)
          Behavior.same
        case CurrencyRequestFailure(ex) =>
          ctx.log.error(ex, s"Connection failed to actor")
          Behavior.stopped(() => ctx.log.error(ex, "Actor failed"))
        case CurrencyRequestClosed =>
          ctx.log.debug(s"Connection closed to actor connection")
          Behavior.stopped
      }
      .receiveSignal {
        case (_, t: Terminated) =>
          ctx.log.debug(s"Actor terminated ${t.ref.path}")
          Behaviors.same
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
