package org.github.ainr.todo_backend

import cats.data.Kleisli
import cats.effect.{ConcurrentEffect, Timer}
import org.github.ainr.todo_backend.config.Http
import org.http4s.{Request, Response}
import org.http4s.server.blaze.BlazeServerBuilder

import scala.concurrent.ExecutionContext

package object http {

  def server[
    F[_]
    : Timer
    : ConcurrentEffect
  ](
   conf: Http.Config
  )(
    service: Kleisli[F, Request[F], Response[F]]
  )(
    ec: ExecutionContext
  ): F[Unit] = {
    BlazeServerBuilder[F](ec)
      .bindHttp(conf.port)
      .withHttpApp(service)
      .serve
      .compile
      .drain
  }
}
