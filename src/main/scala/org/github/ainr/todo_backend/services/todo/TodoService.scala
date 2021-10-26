package org.github.ainr.todo_backend.services.todo

import org.github.ainr.todo_backend.domain.{Id, TodoItem, TodoPayload}
import org.github.ainr.todo_backend.services.todo.TodoService.TodoServiceError


trait TodoService[F[_]] {

  def getAllTodoItems: F[List[TodoItem]]

  def getTodoItemById(id: Id): F[Option[TodoItem]]

  def createTodoItem(todo: TodoPayload): F[TodoItem]

  def changeTodoItemById(
    id: Id,
    title: Option[String],
    completed: Option[Boolean],
    ordering: Option[Int]
  ): F[Either[TodoServiceError, TodoItem]]

  def deleteAllTodoItems(): F[Unit]

  def deleteTodoItemById(id: Id): F[Unit]

}

object TodoService {
  sealed trait TodoServiceError
  final case object TodoItemNotFound extends TodoServiceError
}