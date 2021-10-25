package org.github.ainr.todo_backend.infrastructure.metrics

import cats.Applicative
import cats.syntax.all.*
import io.prometheus.client.{CollectorRegistry, Counter}


final case class LogsCounter[F[_]](
  errorCounter: F[Counter],
  warnCounter: F[Counter],
  infoCounter: F[Counter],
  debugCounter: F[Counter]
)

object LogsCounter {

  def apply[
    F[_]
    : Applicative
  ](
    registry: CollectorRegistry
  ): LogsCounter[F] = LogsCounter(
      errorCounter.register(registry).pure[F],
      warnCounter.register(registry).pure[F],
      infoCounter.register(registry).pure[F],
      debugCounter.register(registry).pure[F],
    )

  private lazy val errorCounter: Counter.Builder =
    Counter
      .build()
      .name("log_error")
      .help("Total error logs")
      .labelNames("label")

  private lazy val warnCounter: Counter.Builder =
    Counter
      .build()
      .name("log_warn")
      .help("Total warning logs")
      .labelNames("label")

  private lazy val infoCounter: Counter.Builder =
    Counter
      .build()
      .name("log_info")
      .help("Total info logs")
      .labelNames("label")

  private lazy val debugCounter: Counter.Builder =
    Counter
      .build()
      .name("log_debug")
      .help("Total debug logs")
      .labelNames("label")
}
