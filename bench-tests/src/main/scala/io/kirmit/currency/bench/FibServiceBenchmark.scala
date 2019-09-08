package io.kirmit.currency.bench

import java.util.concurrent.TimeUnit

import io.kirmit.currency.service.FibonacciSequence
import org.openjdk.jmh.annotations._

@State(Scope.Thread)
@Warmup(iterations = 15, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 15, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(
  value = 1,
  jvmArgs = Array(
    "-server",
    "-Xms2g",
    "-Xmx2g",
    "-XX:NewSize=1g",
    "-XX:MaxNewSize=1g",
    "-XX:InitialCodeCacheSize=512m",
    "-XX:ReservedCodeCacheSize=512m",
    "-XX:+UseParallelGC",
    "-XX:-UseBiasedLocking"
  )
)
@BenchmarkMode(Array(Mode.Throughput))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class FibServiceBenchmark {

  var service: FibonacciSequence = _

  @Setup(Level.Iteration)
  def setup() = {
    service = FibonacciSequence()
  }

  @Benchmark
  def calculateFib100Idx(): BigInt = service.row(1000)
}
