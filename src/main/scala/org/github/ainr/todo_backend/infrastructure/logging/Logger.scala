package org.github.ainr.todo_backend.infrastructure.logging

import cats.Applicative
import cats.syntax.all._
import org.slf4j.{LoggerFactory, Logger as slf4jLogger}

import scala.language.implicitConversions

trait Logger[F[_]] {

  def error(msg: String): F[Unit]

  def error(msg: String, err: Throwable): F[Unit]

  def warn(msg: String): F[Unit]

  def info(msg: String): F[Unit]

  def debug(msg: String): F[Unit]
}

object Logger {

  def apply[
    F[_]
  ](
    implicit
    logger: Logger[F]
  ): Logger[F] = logger

  implicit def instance[
    F[_]
    : Applicative
  ](
    clazz: Class[?]
  )(
    implicit
    logger: slf4jLogger = LoggerFactory.getLogger(clazz)
  ): Logger[F] = new Logger[F] {

    def error(msg: String): F[Unit] = logger.error(msg).pure[F]

    def error(msg: String, err: Throwable): F[Unit] = logger.error(msg, err).pure[F]

    def warn (msg: String): F[Unit] = logger.warn(msg).pure[F]

    def info (msg: String): F[Unit] = logger.info(msg).pure[F]

    def debug(msg: String): F[Unit] = logger.debug(msg).pure[F]
  }

  implicit def instance[
    F[_]
    : Applicative
  ](
    implicit logger: slf4jLogger
  ): Logger[F] = new Logger[F] {

    def error(msg: String): F[Unit] = logger.error(msg).pure[F]

    def error(msg: String, err: Throwable): F[Unit] = logger.error(msg, err).pure[F]

    def warn (msg: String): F[Unit] = logger.warn(msg).pure[F]

    def info (msg: String): F[Unit] = logger.info(msg).pure[F]

    def debug(msg: String): F[Unit] = logger.debug(msg).pure[F]
  }
}
