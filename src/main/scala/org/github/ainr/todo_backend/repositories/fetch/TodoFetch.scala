package org.github.ainr.todo_backend.repositories.fetch

import cats.syntax.all.*
import cats.effect.Concurrent
import fetch.{Data, DataSource}
import org.github.ainr.todo_backend.infrastructure.logging.interpreters.Logger
import org.github.ainr.todo_backend.infrastructure.logging.interpreters.Logger.*
import org.github.ainr.todo_backend.infrastructure.logging.LazyLogging
import org.github.ainr.todo_backend.repositories.TodoRepo
import org.github.ainr.todo_backend.services.todo.domain.Message


object TodoFetch extends Data[Int, Message] {

  override def name: String = "FetchMessages"

  def source[F[_]: Concurrent](
    repo: TodoRepo[F]
  ): DataSource[F, Int, Message] =

    new DataSource[F, Int, Message] with LazyLogging {

    override def data: Data[Int, Message] = TodoFetch

    override def CF: Concurrent[F] = Concurrent[F]

    override def fetch(id: Int): F[Option[Message]] = {
      repo.selectRandomMessage() <* Logger[F].info(s"Fetch message from repo $id")
    }
  }
}
