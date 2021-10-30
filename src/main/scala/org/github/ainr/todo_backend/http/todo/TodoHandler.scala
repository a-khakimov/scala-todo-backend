package org.github.ainr.todo_backend.http.todo

import cats.Applicative
import cats.syntax.all._
import org.github.ainr.todo_backend.domain.Id
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
      ).getOrElse(NotFound.asLeft)
    } yield response

    override def createTodoItem(request: TodoPostRequest): F[Either[ErrorInfo, TodoResponse]] =
      todoService
        .createTodoItem(request.asTodoPayload)
        .map(item => TodoResponse("http://localhost:5555/api", item).asRight)

    override def changeTodoItemById(
      request: (Long, TodoPatchRequest)
    ): F[Either[ErrorInfo, TodoResponse]] = {
      val (id, req) = request
      todoService
        .changeTodoItemById(Id(id), req.title, req.completed, req.order)
        .map {
          case Right(item) => TodoResponse("http://localhost:5555/api", item).asRight
          case Left(_) => NotFound.asLeft
        }
    }

    override def deleteAllTodoItems(): F[Either[ErrorInfo, Unit]] =
      todoService.deleteAllTodoItems().map(_.asRight)

    override def deleteTodoItemById(id: Long): F[Either[ErrorInfo, Unit]] =
      todoService.deleteTodoItemById(Id(id)).map {
        case Right(_) => ().asRight
        case Left(_) => NotFound.asLeft
      }
  }
}