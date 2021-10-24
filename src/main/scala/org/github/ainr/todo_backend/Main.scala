package org.github.ainr.todo_backend

import cats.effect.{Async, Blocker, ContextShift, ExitCode, IO, IOApp, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.github.ainr.todo_backend.config.AppConfig
import org.github.ainr.todo_backend.http.interpreter.HandlerImpl
import org.github.ainr.todo_backend.infrastructure.logging.LazyLogging
import org.github.ainr.todo_backend.infrastructure.logging.interpreters.Logger.instance
import org.github.ainr.todo_backend.infrastructure.logging.interpreters.{Logger, LoggerWithMetrics}
import org.github.ainr.todo_backend.infrastructure.metrics.LoggerCounters
import org.github.ainr.todo_backend.repositories.fetch.TodoFetch
import org.github.ainr.todo_backend.repositories.{TodoRepo, TodoRepoDoobieImpl}
import org.github.ainr.todo_backend.services.healthcheck.{HealthCheckService, HealthCheckServiceImpl}
import org.github.ainr.todo_backend.services.todo.{TodoService, TodoServiceImpl}
import org.github.ainr.todo_backend.services.version.VersionService
import org.github.ainr.todo_backend.services.version.interpreter.VersionServiceImpl
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.metrics.MetricsOps
import org.http4s.metrics.prometheus.{Prometheus, PrometheusExportService}
import org.http4s.server.Router
import org.http4s.server.middleware.{CORS, Metrics}
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext


object Main extends IOApp with LazyLogging {

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- Logger[IO].info("Application running")
      config <- AppConfig.load[IO]
      _ <- Logger[IO].info(s"${config.http}")
      _ <- Logger[IO].info(s"${config.database}")
      _ <- db.migrate[IO](config.database)
      _ <- resources[IO](config).use {
        case (ec, transactor, metricsService, metrics) => {

          val loggerCounters = LoggerCounters[IO](metricsService.collectorRegistry)

          val healthCheckServiceLogger = new LoggerWithMetrics[IO](LoggerFactory.getLogger(HealthCheckService.getClass))(loggerCounters)
          val messagesServiceLogger = new LoggerWithMetrics[IO](LoggerFactory.getLogger(TodoService.getClass))(loggerCounters)
          val versionServiceLogger = new LoggerWithMetrics[IO](LoggerFactory.getLogger(VersionService.getClass))(loggerCounters)
          val messagesRepoLogger = new LoggerWithMetrics[IO](LoggerFactory.getLogger(TodoRepo.getClass))(loggerCounters)

          val repo: TodoRepo[IO] = new TodoRepoDoobieImpl(transactor)(messagesRepoLogger)
          val healthCheckService: HealthCheckService[IO] = new HealthCheckServiceImpl[IO](healthCheckServiceLogger)
          val messagesService: TodoService[IO] = new TodoServiceImpl[IO](repo, TodoFetch.source(repo))(messagesServiceLogger)
          val versionService: VersionService[IO] = new VersionServiceImpl[IO](versionServiceLogger)

          val handler: http.Handler[IO] = new HandlerImpl[IO](messagesService, healthCheckService, versionService)

          val router = Router[IO](
            "/api" -> Metrics[IO](metrics)(handler.routes),
            "/" -> metricsService.routes
          )

          http.server(CORS(router).orNotFound)(ec)
        }
      }
    } yield ExitCode.Success
  }

  def resources[F[_]: Async: ContextShift](
    config: AppConfig.Config
  ): Resource[F, (ExecutionContext, HikariTransactor[F], PrometheusExportService[F],  MetricsOps[F])] = {
    for {
      blocker <- Blocker[F]
      metricsService <- PrometheusExportService.build[F]
      metrics <- Prometheus.metricsOps[F](metricsService.collectorRegistry, "server")
      ec <- ExecutionContexts.cachedThreadPool[F]
      transactor <- db.transactor[F](config.database)(ec, blocker)
    } yield (ec, transactor, metricsService, metrics)
  }
}
