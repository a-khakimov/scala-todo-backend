package org.github.ainr.todo_backend.services.version

import VersionService.Version


trait VersionService[F[_]] {

  def version(): F[Version]
}

object VersionService {

  final case class Version(version: String)
}



