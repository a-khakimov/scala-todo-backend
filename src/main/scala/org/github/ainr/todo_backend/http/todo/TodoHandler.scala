package org.github.ainr.todo_backend.http.todo

import cats.Functor
import cats.syntax.all.*
import org.github.ainr.todo_backend.http.interpreter.HandlerImpl.TodoResponse
import org.github.ainr.todo_backend.http.todo.endpoints.ErrorInfo
import org.github.ainr.todo_backend.services.todo.TodoService

trait TodoHandler[F[_]] {

  def getAllTodoItems(): F[Either[ErrorInfo, List[TodoResponse]]]
}

object TodoHandler {
  def apply[
    F[_]
    : Functor
  ](
    todoService: TodoService[F]
  ): TodoHandler[F] = {
    new TodoHandlerImpl[F](todoService)
  }

  final class TodoHandlerImpl[
    F[_]
    : Functor
  ](
    todoService: TodoService[F]
  ) extends TodoHandler[F] {

    def getAllTodoItems(): F[Either[ErrorInfo, List[TodoResponse]]] = for {
      items <- todoService.getAllTodoItems()
      response = items.map(item =>
        TodoResponse("http://localhost:5555/api", item)
      ).asRight
    } yield response

    //def getAllTodoItems(): F[Either[ErrorInfo, List[TodoResponse]]] = for {
    //  _ <- todoService.getAllTodoItems()
    //  response = Unauthorized("Hui").asLeft
    //} yield response
  }
}