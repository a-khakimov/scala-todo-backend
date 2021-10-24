package org.github.ainr.todo_backend.http

import org.http4s.HttpRoutes

trait Handler[F[_]] {
  def routes: HttpRoutes[F]
}
