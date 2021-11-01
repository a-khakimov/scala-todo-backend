package org.github.ainr.todo_backend.infrastructure.logging

import cats.Applicative
import cats.syntax.all._
import org.github.ainr.todo_backend.infrastructure.metrics.LogsCounter
import org.slf4j.{Logger => slf4jLogger}


trait LoggerWithMetrics[F[_]]
  extends Logger[F]
    with Labels[F]

object LoggerWithMetrics {
  def apply[
    F[_]
    : Applicative
  ](
    logger: slf4jLogger
  )(
    counters: LogsCounter[F]
  ): LoggerWithMetrics[F] = new LoggerWithMetricsImpl(logger)(counters)

  final class LoggerWithMetricsImpl[
    F[_]
    : Applicative
  ](
     logger: slf4jLogger
   )(
     counters: LogsCounter[F]
   ) extends LoggerWithMetrics[F] {

    override def error(msg: String): F[Unit] =
      logger.error(msg).pure[F] <*
        counters.errorCounter.map(_.labels(s"label_error").inc())

    override def error(msg: String, err: Throwable): F[Unit] =
      logger.error(msg, err).pure[F] <*
        counters.errorCounter.map(_.labels(s"label_error").inc())

    override def warn(msg: String): F[Unit] =
      logger.warn(msg).pure[F] <*
        counters.warnCounter.map(_.labels(s"label_warn").inc())

    override def info(msg: String): F[Unit] =
      logger.info(msg).pure[F] <*
        counters.infoCounter.map(_.labels(s"label_info").inc())

    override def debug(msg: String): F[Unit] =
      logger.debug(msg).pure[F] <*
        counters.debugCounter.map(_.labels(s"label_debug").inc())


    override def error(label: String, msg: String): F[Unit] =
      logger.error(msg).pure[F] <*
        counters.errorCounter.map(_.labels(s"label_$label").inc())

    override def error(label: String, msg: String, err: Throwable): F[Unit] =
      logger.error(msg).pure[F] <*
        counters.errorCounter.map(_.labels(s"label_$label").inc())

    override def warn(label: String, msg: String): F[Unit] =
      logger.warn(msg).pure[F] <*
        counters.warnCounter.map(_.labels(s"label_$label").inc())

    override def info(label: String, msg: String): F[Unit] =
      logger.info(msg).pure[F] <*
        counters.infoCounter.map(_.labels(s"label_$label").inc())

    override def debug(label: String, msg: String): F[Unit] =
      logger.debug(msg).pure[F] <*
        counters.debugCounter.map(_.labels(s"label_$label").inc())
  }
}
