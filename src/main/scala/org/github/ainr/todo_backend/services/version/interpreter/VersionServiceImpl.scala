package org.github.ainr.todo_backend.services.version.interpreter

import cats.syntax.all.*
import cats.Applicative
import org.github.ainr.todo_backend.infrastructure.logging.{Labels, Logger}
import org.github.ainr.todo_backend.services.version.VersionService
import org.github.ainr.todo_backend.services.version.VersionService.Version

final class VersionServiceImpl[
  F[_]
  : Applicative
](
  logger: Logger[F] & Labels[F]
) extends VersionService[F] {

  override def version(): F[Version] =
    Version("0.0.1").pure[F] <*
      logger.info("version", "Version")
}