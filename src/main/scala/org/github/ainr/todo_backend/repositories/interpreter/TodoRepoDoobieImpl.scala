package org.github.ainr.todo_backend.repositories.interpreter

import cats.effect.Bracket
import cats.syntax.all.*
import doobie.implicits.*
import doobie.refined.implicits.*
import doobie.util.fragment.Fragment
import doobie.util.transactor.Transactor
import org.github.ainr.todo_backend.domain.{Id, Title, TodoItem}
import org.github.ainr.todo_backend.infrastructure.logging.{Labels, Logger}
import org.github.ainr.todo_backend.repositories.TodoRepo
import TodoRepoDoobieImpl.SQL

class TodoRepoDoobieImpl[
  F[_]
](
  xa: Transactor[F]
)(
  logger: Logger[F] & Labels[F]
)(
  implicit
  bracket: Bracket[F, Throwable]
) extends TodoRepo[F] {

  override def getAllTodoItems: F[List[TodoItem]] =
    SQL
      .getAllTodoItems
      .query[TodoItem]
      .stream
      .take(100) // todo ???
      .compile
      .toList
      .transact(xa) <* logger.info("")

  override def getTodoItemById(id: Id): F[Option[TodoItem]] =
    SQL
      .getTodoItemById(id)
      .query[TodoItem]
      .option
      .transact(xa) <* logger.info("")

  override def insertTodoItem(title: Title): F[Option[Id]] =
    SQL
      .insertTodoItem(title)
      .query[Id]
      .option
      .transact(xa) <* logger.info("")

  override def updateTodoItem(item: TodoItem): F[Unit] = ???

  override def deleteAllTodoItems(): F[Unit] = ???

  override def deleteTodoItemById(id: Id): F[Unit] = ???
}

object TodoRepoDoobieImpl {

  object SQL {
    def getAllTodoItems: Fragment = sql"""SELECT id, title FROM todo"""

    def getTodoItemById(id: Id): Fragment = sql"""SELECT id, title FROM todo WHERE id=$id"""

    def insertTodoItem(title: Title): Fragment = sql"""INSERT INTO todo (title) VALUES ($title) RETURNING id"""

    def updateTodoItem(item: TodoItem): Fragment = sql"""UPDATE todo SET title = ${item.title} where id = ${item.id}"""

    def deleteAllTodoItems(): Fragment = sql"""TRUNCATE todo"""

    def deleteTodoItemById(id: Id): Fragment = sql"""DELETE from todo WHERE id = $id"""
  }
}


