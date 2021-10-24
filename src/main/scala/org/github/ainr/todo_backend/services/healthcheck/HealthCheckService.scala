package org.github.ainr.todo_backend.services.healthcheck

import org.github.ainr.todo_backend.services.healthcheck.HealthCheckService.HealthCheckData

trait HealthCheckService[F[_]] {
  def healthCheck(): F[HealthCheckData]
}

object HealthCheckService {
  case class HealthCheckData(message: String)
}
