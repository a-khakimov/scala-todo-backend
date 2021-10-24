package org.github.ainr.todo_backend.http.interpreter

import cats.effect.Sync
import org.github.ainr.todo_backend.http.Handler
import org.github.ainr.todo_backend.services.healthcheck.HealthCheckService
import org.github.ainr.todo_backend.services.todo.TodoService
import org.github.ainr.todo_backend.services.version.VersionService
import org.http4s.HttpRoutes
import org.http4s.dsl.Http4sDsl

final class HandlerImpl[
  F[_]
  : Sync
](
   todoService: TodoService[F],
   healthCheckService: HealthCheckService[F],
   versionService: VersionService[F]
 ) extends Handler[F] {

  object dsl extends Http4sDsl[F]
  import dsl.*

  override def routes: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root => Ok()
    case GET -> Root / todoItemId => Ok(todoItemId)
    case POST -> Root => Ok()
    case POST -> Root / id => Ok(id)
    case DELETE -> Root => Ok()
    case DELETE -> Root / id => Ok(id)
    case PATCH -> Root => Ok()
    case PATCH -> Root / id => Ok(id)
  }
}