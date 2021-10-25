package org.github.ainr.todo_backend.services.todo.interpreter

import cats.syntax.all.*
import cats.effect.Concurrent
import org.github.ainr.todo_backend.domain.{Id, TodoItem, TodoPayload}
import org.github.ainr.todo_backend.infrastructure.logging.{Labels, Logger}
import org.github.ainr.todo_backend.repositories.TodoRepo
import org.github.ainr.todo_backend.services.todo.TodoService

final class TodoServiceImpl[
  F[_]
  : Concurrent
](
  repo: TodoRepo[F]
)(
  logger: Logger[F] & Labels[F]
) extends TodoService[F] {

  override def getAllTodoItems: F[List[TodoItem]] =
    repo.getAllTodoItems <* logger.info("Get all todo items")

  override def getTodoItemById(id: Id): F[Option[TodoItem]] =
    repo.getTodoItemById(id) <* logger.info("")

  override def createTodoItem(todo: TodoPayload): F[TodoItem] =
    repo.createTodoItem(todo) <* logger.info("")

  override def changeTodoItemById(item: TodoItem): F[Unit] =
    repo.updateTodoItem(item) <* logger.info("")

  override def deleteAllTodoItems(): F[Unit] =
    repo.deleteAllTodoItems() <* logger.info("")

  override def deleteTodoItemById(id: Id): F[Unit] =
    repo.deleteTodoItemById(id) <* logger.info("")
}

