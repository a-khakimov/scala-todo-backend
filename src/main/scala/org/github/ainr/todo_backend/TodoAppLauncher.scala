package org.github.ainr.todo_backend

import cats.effect.{Async, Blocker, ContextShift, ExitCode, IO, IOApp, Resource}
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import org.github.ainr.todo_backend.config.AppConfig
import org.github.ainr.todo_backend.http.interpreter.HandlerImpl
import org.github.ainr.todo_backend.http.todo.{TodoHandler, endpoints}
import org.github.ainr.todo_backend.infrastructure.logging.LazyLogging
import org.github.ainr.todo_backend.infrastructure.logging.interpreters.Logger.instance
import org.github.ainr.todo_backend.infrastructure.logging.interpreters.{Logger, LoggerWithMetrics}
import org.github.ainr.todo_backend.infrastructure.metrics.LogsCounter
import org.github.ainr.todo_backend.repositories.TodoRepo
import org.github.ainr.todo_backend.repositories.interpreter.TodoRepoDoobieImpl
import org.github.ainr.todo_backend.services.healthcheck.HealthCheckService
import org.github.ainr.todo_backend.services.healthcheck.interpreter.HealthCheckServiceImpl
import org.github.ainr.todo_backend.services.todo.TodoService
import org.github.ainr.todo_backend.services.todo.interpreter.TodoServiceImpl
import org.github.ainr.todo_backend.services.version.VersionService
import org.github.ainr.todo_backend.services.version.interpreter.VersionServiceImpl
import org.http4s.implicits.http4sKleisliResponseSyntaxOptionT
import org.http4s.metrics.MetricsOps
import org.http4s.metrics.prometheus.{Prometheus, PrometheusExportService}
import org.http4s.server.Router
import org.http4s.server.middleware.{CORS, Metrics}
import org.slf4j.LoggerFactory
import sttp.tapir.server.http4s.Http4sServerInterpreter

import scala.concurrent.ExecutionContext


object TodoAppLauncher extends IOApp with LazyLogging {

  override def run(args: List[String]): IO[ExitCode] = {
    for {
      _ <- Logger[IO].info("Application running")
      config <- AppConfig.load[IO]
      _ <- Logger[IO].info(s"${config.http}")
      _ <- Logger[IO].info(s"${config.database}")
      _ <- db.migrate[IO](config.database)
      _ <- resources[IO](config).use {
        case (ec, transactor, metricsService, metrics) =>

          val logsCounter: LogsCounter[IO] = LogsCounter[IO](metricsService.collectorRegistry)

          val serviceLogger = new LoggerWithMetrics[IO](LoggerFactory.getLogger(LogsCounter.getClass))(logsCounter)
          val versionServiceLogger = new LoggerWithMetrics[IO](LoggerFactory.getLogger(LogsCounter.getClass))(logsCounter)

          val todoRepo: TodoRepo[IO] = TodoRepoDoobieImpl(transactor, logsCounter)
          val healthCheckService: HealthCheckService[IO] = HealthCheckServiceImpl[IO](logsCounter)
          val todoService: TodoService[IO] = new TodoServiceImpl[IO](todoRepo)(serviceLogger)
          val versionService: VersionService[IO] = new VersionServiceImpl[IO](versionServiceLogger)

          val handler: http.Handler[IO] = new HandlerImpl[IO](todoService, healthCheckService, versionService)
          val todoHandler = TodoHandler[IO](todoService)

          println(handler)

          import sttp.tapir.openapi.circe.yaml._
          println(endpoints.docs.toYaml)

          val testTapirRoutes =
            Http4sServerInterpreter.toRoutes(endpoints.getAllTodoItems)(_ => todoHandler.getAllTodoItems())

          val router = Router[IO](
            "/api" -> Metrics[IO](metrics)(testTapirRoutes),
            "/" -> metricsService.routes
          )

          http.server(CORS(router).orNotFound)(ec)
      }
    } yield ExitCode.Success
  }

  def resources[
    F[_]
    : Async
    : ContextShift
  ](
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
