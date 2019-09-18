package io.kirmit.load

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class FibonacciPathSimulation extends Simulation {

  val httpProtocol = http
    .baseUrl("http://computer-database.gatling.io") // 5
    .acceptHeader("text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8") // 6
    .doNotTrackHeader("1")
    .acceptLanguageHeader("en-US,en;q=0.5")
    .acceptEncodingHeader("gzip, deflate")
    .userAgentHeader("Mozilla/5.0 (Windows NT 5.1; rv:31.0) Gecko/20100101 Firefox/31.0")

  val scn = scenario("Scenario Name") // A scenario is a chain of requests and pauses
    .exec(http("request_1")
    .get("/"))
    .pause(7) // Note that Gatling has recorder real time pauses

  setUp(scn.inject(atOnceUsers(1)).protocols(httpProtocol))
}
