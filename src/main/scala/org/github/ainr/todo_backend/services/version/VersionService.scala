package org.github.ainr.todo_backend.services.version

trait VersionService[F[_]] {

  def version(): F[Version]
}
