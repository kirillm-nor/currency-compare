package io.kirmit.load

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class FibonacciPathSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://127.0.0.1:8080")
    .acceptHeader("text/html,application/xhtml+xml,application/json;q=0.9,*/*;q=0.8")
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")

  val warmup = scenario("Warmup Scenario")
    .exec(
      http("warn_request_fib_10000")
        .get("/fib")
        .queryParam("idx", 1000)
        .check(status.is(200)))
    .pause(30 seconds)

  val scn = scenario("Request Scenario")
    .exec(
      http("request_fib_10000")
        .get("/fib")
        .queryParam("idx", 1000)
        .check(status.is(200)))

  setUp(
    warmup
      .inject(
        rampUsers(50) during (30 seconds),
        rampUsers(75) during (30 seconds),
        constantUsersPerSec(50) during (30 seconds)
      )
      .protocols(httpProtocol),
    scn
      .inject(
        constantUsersPerSec(100) during (30 seconds)
      )
      .protocols(httpProtocol)
  )
}
