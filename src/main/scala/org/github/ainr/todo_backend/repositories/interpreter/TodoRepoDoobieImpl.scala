package org.github.ainr.todo_backend.repositories.interpreter

import cats.effect.Bracket
import cats.syntax.all.*
import doobie.implicits.*
import doobie.util.transactor.Transactor
import org.github.ainr.todo_backend.domain.{Id, TodoItem, TodoPayload}
import org.github.ainr.todo_backend.infrastructure.logging.interpreters.LoggerWithMetrics
import org.github.ainr.todo_backend.infrastructure.logging.{Labels, Logger}
import org.github.ainr.todo_backend.infrastructure.metrics.LogsCounter
import org.github.ainr.todo_backend.repositories.TodoRepo
import org.slf4j.LoggerFactory

class TodoRepoDoobieImpl[
  F[_]
](
  xa: Transactor[F],
  logger: Logger[F] & Labels[F]
)(
  implicit
  bracket: Bracket[F, Throwable]
) extends TodoRepo[F] {

  override def getAllTodoItems: F[List[TodoItem]] =
    SQL
      .getAll
      .to[List]
      .transact(xa) <* logger.info("Get all todo items")

  override def getTodoItemById(id: Id): F[Option[TodoItem]] =
    SQL
      .getById(id)
      .option
      .transact(xa)

  override def createTodoItem(todo: TodoPayload): F[TodoItem] =
    SQL
      .create(todo)
      .withUniqueGeneratedKeys[Long]("id")
      .map(id => TodoItem(Id(id), todo))
      .transact(xa)

  override def updateTodoItem(item: TodoItem): F[Unit] =
    SQL
      .update(item)
      .run
      .transact(xa)
      .as(())

  override def deleteAllTodoItems(): F[Unit] =
    SQL
      .deleteAll()
      .run
      .transact(xa)
      .as(())

  override def deleteTodoItemById(id: Id): F[Unit] =
    SQL
      .deleteById(id)
      .run
      .transact(xa)
      .as(())
}

object TodoRepoDoobieImpl {
  def apply[F[_]](
    transactor: Transactor[F],
    logsCounter: LogsCounter[F]
  )(
    implicit
    bracket: Bracket[F, Throwable]
  ): TodoRepo[F] = {
    val logger = new LoggerWithMetrics[F](LoggerFactory.getLogger(TodoRepoDoobieImpl.getClass))(logsCounter)
    new TodoRepoDoobieImpl(transactor, logger)
  }
}

object SQL {
  def getAll: doobie.Query0[TodoItem] =
    sql"""SELECT id, title, completed, ordering
          FROM todo
          """.query[TodoItem]

  def getById(id: Id): doobie.Query0[TodoItem] =
    sql"""SELECT id, title, completed, ordering
          FROM todo WHERE id = ${id.value}
          """.query[TodoItem]

  def create(todo: TodoPayload): doobie.Update0 =
    sql"""INSERT INTO todo (title, completed, ordering)
          VALUES (${todo.title}, ${todo.completed}, ${todo.ordering})
          """.update

  def update(item: TodoItem): doobie.Update0 =
    sql"""UPDATE todo SET
          title = ${item.payload.title},
          completed = ${item.payload.completed},
          ordering = ${item.payload.ordering}
          WHERE id = ${item.id.value}
          """.update

  def deleteAll(): doobie.Update0 =
    sql"""TRUNCATE TABLE todo""".update

  def deleteById(id: Id): doobie.Update0 =
    sql"""DELETE from todo
          WHERE id = ${id.value}
          """.update
}
