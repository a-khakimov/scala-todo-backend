package org.github.ainr.todo_backend.services.todo.interpreter

import cats.syntax.all._
import cats.effect.Concurrent
import org.github.ainr.todo_backend.domain.{Id, TodoItem, TodoPayload}
import org.github.ainr.todo_backend.infrastructure.logging.{Labels, Logger}
import org.github.ainr.todo_backend.repositories.TodoRepo
import org.github.ainr.todo_backend.services.todo.TodoService
import org.github.ainr.todo_backend.services.todo.TodoService.{TodoItemNotFound, TodoServiceError}

final class TodoServiceImpl[
  F[_]
  : Concurrent
](
  repo: TodoRepo[F]
)(
  logger: Logger[F] & Labels[F]
) extends TodoService[F] {

  override def getAllTodoItems(): F[List[TodoItem]] =
    repo.getAllTodoItems <* logger.info("Get all todo items")

  override def getTodoItemById(id: Id): F[Option[TodoItem]] =
    repo.getTodoItemById(id) <* logger.info("")

  override def createTodoItem(todo: TodoPayload): F[TodoItem] =
    repo.createTodoItem(todo) <* logger.info("")

  override def changeTodoItemById(
    id: Id,
    title: Option[String],
    completed: Option[Boolean],
    ordering: Option[Int]
  ): F[Either[TodoServiceError, TodoItem]] = {

    def newTodoPayload(default: TodoPayload,
               title: Option[String],
               completed: Option[Boolean],
               ordering: Option[Int]): TodoPayload = {
      TodoPayload(
        title.getOrElse(default.title),
        completed.getOrElse(default.completed),
        ordering
      )
    }
    for {
      oldItem <- repo.getTodoItemById(id)
      newItem <- oldItem.map(
        item => TodoItem(id, newTodoPayload(item.payload, title, completed, ordering))
      ).pure[F]
      result <- newItem match {
        case Some(item) => repo.updateTodoItem(item) *> Right(item).pure[F]
        case None => Left(TodoItemNotFound).pure[F]
      }
      _ <- logger.info("")
    } yield result
  }

  override def deleteAllTodoItems(): F[Unit] =
    repo.deleteAllTodoItems() <* logger.info("")

  override def deleteTodoItemById(id: Id): F[Unit] =
    repo.deleteTodoItemById(id) <* logger.info("")
}

