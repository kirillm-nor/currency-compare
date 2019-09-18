package io.kirmit.currency.service

import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.{ActorRef, Behavior}
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
import io.kirmit.currency.service.RequestActorService.{CurrencyResponse, CurrencyResponseDone, CurrencyResponseFailure}
import io.kirmit.currency.service.RequestProcessorActor.RequestWithSink

import scala.concurrent.Future
import scala.util.{Failure, Success}

class RequestProcessorActor(handler: HttpRequest => Future[HttpResponse]) {

  lazy val requestBehavior: Behavior[RequestWithSink] = Behaviors.receive {
    case (ctx, RequestWithSink(request, out)) =>
      ctx.log.debug(s"Received request $request to ${out.path}")
      handler(request).onComplete {
        case Success(response) =>
          ctx.log.debug(s"Request completed $request to ${out.path} with $response")
          out ! CurrencyResponseDone(response)
        case Failure(ex)       =>
          ctx.log.error(ex, s"Request failed $request to ${out.path}")
          out ! CurrencyResponseFailure(ex)
      }(ctx.executionContext)
      Behavior.stopped
  }

}

object RequestProcessorActor {
  case class RequestWithSink(request: HttpRequest, out: ActorRef[CurrencyResponse])

  def apply(handler: HttpRequest => Future[HttpResponse]): RequestProcessorActor = new RequestProcessorActor(handler)

  def behavior(implicit processorActor: RequestProcessorActor): Behavior[RequestWithSink] = processorActor.requestBehavior

}
