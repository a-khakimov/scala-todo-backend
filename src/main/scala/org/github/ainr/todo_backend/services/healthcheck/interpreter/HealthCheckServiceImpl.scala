package org.github.ainr.todo_backend.services.healthcheck.interpreter

import cats.Applicative
import cats.syntax.all.*
import org.github.ainr.todo_backend.infrastructure.logging.{Labels, Logger}
import org.github.ainr.todo_backend.services.healthcheck.HealthCheckService
import org.github.ainr.todo_backend.services.healthcheck.HealthCheckService.HealthCheckData

final class HealthCheckServiceImpl[
  F[_]
  : Applicative
](
  logger: Logger[F] & Labels[F]
) extends HealthCheckService[F] {

  override def healthCheck(): F[HealthCheckData] = {
    HealthCheckData("Hello, my little pony!").pure[F] <*
      logger.info("health_check", "Health checking")
  }
}
