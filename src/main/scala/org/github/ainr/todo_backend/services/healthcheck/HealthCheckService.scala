package org.github.ainr.todo_backend.services.healthcheck

import cats.Applicative
import cats.syntax.all._
import org.github.ainr.todo_backend.infrastructure.logging.{Labels, Logger}
import org.github.ainr.todo_backend.services.healthcheck.HealthCheckService.HealthCheckData

trait HealthCheckService[F[_]] {
  def healthCheck(): F[HealthCheckData]
}

object HealthCheckService {
  case class HealthCheckData(message: String)
}

final class HealthCheckServiceImpl[F[_]: Applicative](
  logger: Logger[F] with Labels[F]
) extends HealthCheckService[F] {

  override def healthCheck(): F[HealthCheckData] = {
    HealthCheckData("Hello, my little pony!").pure[F] <*
      logger.info("health_check", "Health checking")
  }
}
