package org.github.ainr.todo_backend.services.healthcheck.interpreter

import cats.Applicative
import cats.syntax.all.*
import org.github.ainr.todo_backend.infrastructure.logging.interpreters.LoggerWithMetrics
import org.github.ainr.todo_backend.infrastructure.logging.{Labels, Logger}
import org.github.ainr.todo_backend.infrastructure.metrics.LogsCounter
import org.github.ainr.todo_backend.services.healthcheck.HealthCheckService
import org.github.ainr.todo_backend.services.healthcheck.HealthCheckService.HealthCheckData
import org.slf4j.LoggerFactory

final class HealthCheckServiceImpl[
  F[_]
  : Applicative
](
  logger: Logger[F] & Labels[F]
) extends HealthCheckService[F] {

  override def healthCheck(): F[HealthCheckData] = {
    HealthCheckData("Hello, my little pony!").pure[F] <*
      logger.info("Health check")
  }
}

object HealthCheckServiceImpl {
  def apply[
    F[_]
    : Applicative
  ](
    logsCounter: LogsCounter[F]
  ): HealthCheckService[F] = {
    val logger = new LoggerWithMetrics[F](
      LoggerFactory.getLogger(HealthCheckServiceImpl.getClass)
    )(logsCounter)

    new HealthCheckServiceImpl(logger)
  }
}