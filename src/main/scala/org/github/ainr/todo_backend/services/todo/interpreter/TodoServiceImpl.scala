package org.github.ainr.todo_backend.services.todo.interpreter

import cats.effect.{Concurrent, Timer}
import org.github.ainr.todo_backend.domain.{Id, Title, TodoItem}
import org.github.ainr.todo_backend.infrastructure.logging.{Labels, Logger}
import org.github.ainr.todo_backend.repositories.TodoRepo
import org.github.ainr.todo_backend.services.todo.TodoService

final class TodoServiceImpl[
  F[_]
  : Concurrent
  : Timer
](
  repo: TodoRepo[F]
)(
  logger: Logger[F] & Labels[F]
) extends TodoService[F] {

  override def getAllTodoItems: F[List[TodoItem]] = ???

  override def getTodoItemById(id: Id): F[Option[TodoItem]] = ???

  override def createTodoItem(title: Title): F[Unit] = ???

  override def changeTodoItemById(item: TodoItem): F[Option[TodoItem]] = ???

  override def deleteAllTodoItems(): F[Unit] = ???

  override def deleteTodoItemById(id: Id): F[Unit] = ???
}

