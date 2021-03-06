package org.github.ainr.todo_backend

import cats.effect.{Async, Blocker, ContextShift, ExitCode, IO, IOApp, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.github.ainr.todo_backend.config.App
import org.github.ainr.todo_backend.http.todo.{TodoHandler, TodoHttp4sRoutes, endpoints}
import org.github.ainr.todo_backend.infrastructure.logging.{LazyLogging, Logger}
import org.github.ainr.todo_backend.infrastructure.metrics.LogsCounter
import org.github.ainr.todo_backend.repositories.todo.TodoRepo
import org.github.ainr.todo_backend.services.todo.TodoService
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.metrics.MetricsOps
import org.http4s.metrics.prometheus.{Prometheus, PrometheusExportService}
import org.http4s.server.Router
import org.http4s.server.middleware.Metrics
import sttp.tapir.swagger.http4s.SwaggerHttp4s

import scala.concurrent.ExecutionContext


object TodoAppLauncher extends IOApp with LazyLogging {

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- Logger[IO].info("Application running")
      config <- App.load[IO]
      _ <- Logger[IO].info(s"${config.http}")
      _ <- Logger[IO].info(s"${config.database}")
      _ <- db.migrate[IO](config.database)
      _ <- resources[IO](config).use {
        case (ec, transactor, metricsService, metrics) =>

          val logsCounter: LogsCounter[IO] = LogsCounter[IO](metricsService.collectorRegistry)

          val todoRepo: TodoRepo[IO] = TodoRepo(transactor, logsCounter)
          val todoService: TodoService[IO] = TodoService(todoRepo)(logsCounter)
          val todoHandler: TodoHandler[IO] = TodoHandler(todoService, config.http)

          val router = Router[IO](
            "/api" -> Metrics[IO](metrics)(TodoHttp4sRoutes(todoHandler)),
            "/" -> new SwaggerHttp4s(endpoints.openApiYaml(config.http), "swagger").routes,
            "/metrics" -> metricsService.routes
          ).orNotFound

          http.server(config.http)(router)(ec)
      }
    } yield ExitCode.Success
  }

  def resources[
    F[_]
    : Async
    : ContextShift
  ](
    config: App.Config
  ): Resource[F, (ExecutionContext, HikariTransactor[F], PrometheusExportService[F],  MetricsOps[F])] = {
    for {
      blocker <- Blocker[F]
      ec <- ExecutionContexts.cachedThreadPool[F]
      transactor <- db.transactor[F](config.database)(ec, blocker)
      metricsService <- PrometheusExportService.build[F]
      metrics <- Prometheus.metricsOps[F](metricsService.collectorRegistry, "server")
    } yield (ec, transactor, metricsService, metrics)
  }
}
