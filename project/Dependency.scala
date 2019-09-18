import sbt._

object Version {
  val akka        = "2.5.25"
  val akkaHttp    = "10.1.9"
  val circeJson   = "0.12.0-RC4"
  val gatling     = "3.2.1"
  val jdk         = "1.8"
  val kamon_2_0_1 = "2.0.1"
  val kamon_2_0_0 = "2.0.0"
}

object Library {
  val akkaActor         = "com.typesafe.akka"          %% "akka-actor-typed"         % Version.akka
  val akkaStream        = "com.typesafe.akka"          %% "akka-stream-typed"        % Version.akka
  val akkaHttpCore      = "com.typesafe.akka"          %% "akka-http-core"           % Version.akkaHttp
  val akkaHttp          = "com.typesafe.akka"          %% "akka-http"                % Version.akkaHttp
  val akkaSlf4j         = "com.typesafe.akka"          %% "akka-slf4j"               % Version.akka
  val circeCore         = "io.circe"                   %% "circe-core"               % Version.circeJson
  val circeGeneric      = "io.circe"                   %% "circe-generic"            % Version.circeJson
  val circeParser       = "io.circe"                   %% "circe-parser"             % Version.circeJson
  val scalaTest         = "org.scalatest"              %% "scalatest"                % "3.0.8" % Test
  val akkaTestkit       = "com.typesafe.akka"          %% "akka-testkit"             % Version.akka % Test
  val akkaStreamTestkit = "com.typesafe.akka"          %% "akka-stream-testkit"      % Version.akka % Test
  val gatlingTest       = "io.gatling"                 % "gatling-test-framework"    % Version.gatling % "test,it"
  val gatlingCharts     = "io.gatling.highcharts"      % "gatling-charts-highcharts" % Version.gatling % "test,it"
  val kamonCore         = "io.kamon"                   %% "kamon-core"               % Version.kamon_2_0_0
  val kamonAkka         = "io.kamon"                   %% "kamon-akka"               % Version.kamon_2_0_0
  val kamonAkkaHttp     = "io.kamon"                   %% "kamon-akka-http"          % Version.kamon_2_0_1
  val kamonSystem       = "io.kamon"                   %% "kamon-system-metrics"     % Version.kamon_2_0_0
  val kamonFuture       = "io.kamon"                   %% "kamon-scala-future"       % Version.kamon_2_0_1
  val kamonStatus       = "io.kamon"                   %% "kamon-status-page"        % Version.kamon_2_0_0
  val scalaLogging      = "com.typesafe.scala-logging" %% "scala-logging"            % "3.9.2"
  val logback           = "ch.qos.logback"             % "logback-classic"           % "1.2.3"
}
