package org.github.ainr.todo_backend.http.todo

import cats.Applicative
import cats.syntax.all._
import org.github.ainr.todo_backend.config.Http
import org.github.ainr.todo_backend.domain.Id
import org.github.ainr.todo_backend.http.todo.endpoints.{ErrorInfo, NotFound}
import org.github.ainr.todo_backend.services.todo.TodoService

trait TodoHandler[F[_]] {

  def getAllTodoItems(): F[Either[ErrorInfo, List[TodoResponse]]]

  def getTodoItemById(id: Id): F[Either[ErrorInfo, TodoResponse]]

  def createTodoItem(request: CreateTodoItemRequest): F[Either[ErrorInfo, TodoResponse]]

  def changeTodoItemById(request: (Id, ChangeTodoItemRequest)): F[Either[ErrorInfo, TodoResponse]]

  def deleteAllTodoItems(): F[Either[ErrorInfo, Unit]]

  def deleteTodoItemById(id: Id): F[Either[ErrorInfo, Unit]]
}

object TodoHandler {
  def apply[
    F[_]
    : Applicative
  ](
    todoService: TodoService[F],
    cfg: Http.Config
  ): TodoHandler[F] = {
    new TodoHandlerImpl[F](todoService, cfg)
  }

  final class TodoHandlerImpl[
    F[_]
    : Applicative
  ](
    todoService: TodoService[F],
    cfg: Http.Config
  ) extends TodoHandler[F] {

    private val url = cfg.serverUri

    def getAllTodoItems(): F[Either[ErrorInfo, List[TodoResponse]]] = for {
      items <- todoService.getAllTodoItems()
      response = items.map(item =>
        TodoResponse(url, item)
      ).asRight
    } yield response

    override def getTodoItemById(id: Id): F[Either[ErrorInfo, TodoResponse]] = for {
      maybeItem <- todoService.getTodoItemById(id)
      response = maybeItem.map(
        TodoResponse(url, _).asRight
      ).getOrElse(NotFound.asLeft)
    } yield response

    override def createTodoItem(request: CreateTodoItemRequest): F[Either[ErrorInfo, TodoResponse]] =
      todoService
        .createTodoItem(request.asTodoPayload)
        .map(item => TodoResponse(url, item).asRight)

    override def changeTodoItemById(
      request: (Id, ChangeTodoItemRequest)
    ): F[Either[ErrorInfo, TodoResponse]] = {
      val (id, req) = request
      todoService
        .changeTodoItemById(id, req.title, req.completed, req.order)
        .map {
          case Right(item) => TodoResponse(url, item).asRight
          case Left(_) => NotFound.asLeft
        }
    }

    override def deleteAllTodoItems(): F[Either[ErrorInfo, Unit]] =
      todoService.deleteAllTodoItems().map(_.asRight)

    override def deleteTodoItemById(id: Id): F[Either[ErrorInfo, Unit]] =
      todoService.deleteTodoItemById(id).map {
        case Right(_) => ().asRight
        case Left(_) => NotFound.asLeft
      }
  }
}