package org.github.ainr.todo_backend.services.version

import cats.Applicative
import cats.syntax.all.*
import org.github.ainr.todo_backend.infrastructure.logging.{Labels, Logger}
import org.github.ainr.todo_backend.services.version.VersionService.Version


trait VersionService[F[_]] {

  def version(): F[Version]
}


object VersionService {

  final case class Version(version: String)
}


final class VersionServiceImpl[F[_]: Applicative](
  logger: Logger[F] & Labels[F]
) extends VersionService[F] {

  override def version(): F[Version] =
    Version("0.0.1").pure[F] <*
      logger.info("version", "Version")
}
