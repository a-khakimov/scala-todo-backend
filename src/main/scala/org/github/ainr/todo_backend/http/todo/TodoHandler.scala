package org.github.ainr.todo_backend.http.todo

import cats.Applicative
import cats.syntax.all._
import org.github.ainr.todo_backend.domain.Id
import org.github.ainr.todo_backend.http.interpreter.HandlerImpl.{TodoPatchRequest, TodoPostRequest, TodoResponse}
import org.github.ainr.todo_backend.http.todo.endpoints.{ErrorInfo, NotFound}
import org.github.ainr.todo_backend.services.todo.TodoService

trait TodoHandler[F[_]] {

  def getAllTodoItems(): F[Either[ErrorInfo, List[TodoResponse]]]

  def getTodoItemById(id: Long): F[Either[ErrorInfo, TodoResponse]]

  def createTodoItem(request: TodoPostRequest): F[Either[ErrorInfo, TodoResponse]]

  def changeTodoItemById(request: (Long, TodoPatchRequest)): F[Either[ErrorInfo, TodoResponse]]

  def deleteAllTodoItems(): F[Either[ErrorInfo, Unit]]

  def deleteTodoItemById(id: Long): F[Either[ErrorInfo, Unit]]
}

object TodoHandler {
  def apply[
    F[_]
    : Applicative
  ](
    todoService: TodoService[F]
  ): TodoHandler[F] = {
    new TodoHandlerImpl[F](todoService)
  }

  final class TodoHandlerImpl[
    F[_]
    : Applicative
  ](
    todoService: TodoService[F]
  ) extends TodoHandler[F] {

    def getAllTodoItems(): F[Either[ErrorInfo, List[TodoResponse]]] = for {
      items <- todoService.getAllTodoItems()
      response = items.map(item =>
        TodoResponse("http://localhost:5555/api", item)
      ).asRight
    } yield response

    override def getTodoItemById(id: Long): F[Either[ErrorInfo, TodoResponse]] = for {
      maybeItem <- todoService.getTodoItemById(Id(id))
      response = maybeItem.map(
        TodoResponse("http://localhost:5555/api", _).asRight
      ).getOrElse(
        NotFound("Hui").asLeft
      )
    } yield response

    override def createTodoItem(request: TodoPostRequest): F[Either[ErrorInfo, TodoResponse]] =
      (NotFound("Hui") : ErrorInfo).asLeft[TodoResponse].pure[F]

    override def changeTodoItemById(request: (Long, TodoPatchRequest)): F[Either[ErrorInfo, TodoResponse]] =
      (NotFound("Hui") : ErrorInfo).asLeft[TodoResponse].pure[F]

    override def deleteAllTodoItems(): F[Either[ErrorInfo, Unit]] =
      (NotFound("Hui") : ErrorInfo).asLeft[Unit].pure[F]

    override def deleteTodoItemById(id: Long): F[Either[ErrorInfo, Unit]] =
      (NotFound("Hui") : ErrorInfo).asLeft[Unit].pure[F]
  }
}