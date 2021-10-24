package org.github.ainr.todo_backend.repositories

import cats.effect.Bracket
import cats.syntax.all.*
import doobie.implicits.*
import doobie.refined.implicits.*
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import org.github.ainr.todo_backend.infrastructure.logging.{Labels, Logger}
import org.github.ainr.todo_backend.services.todo.domain.Message

trait TodoRepo[F[_]] {

  def insertMessage(message: Message): F[Unit]

  def selectRandomMessage(): F[Option[Message]]
}

object TodoRepo {

}

class TodoRepoDoobieImpl[F[_]](
  xa: Transactor[F]
)(
  logger: Logger[F] & Labels[F]
)(
  implicit bracket: Bracket[F, Throwable]
) extends TodoRepo[F] {

  override def insertMessage(message: Message): F[Unit] = {
    for {
      _ <- SQL
        .insertMessage(message)
        .update
        .withUniqueGeneratedKeys[Long]("id")
        .transact(xa)
    } yield ()
  }

  override def selectRandomMessage(): F[Option[Message]] = {
    for {
      message <- SQL
        .selectRandomMessage
        .query[Message]
        .option
        .transact(xa) <* logger.info("get_random_message_DB", "Get random message from DB")
    } yield message
  }
}

object SQL {
  def insertMessage(message: Message): Fragment = {
    sql"""INSERT INTO messages (message) VALUES ($message)"""
  }
  def selectRandomMessage: Fragment = {
    sql"""SELECT message FROM messages ORDER BY RANDOM() LIMIT 1"""
  }
}
