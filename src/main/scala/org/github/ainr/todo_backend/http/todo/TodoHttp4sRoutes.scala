package org.github.ainr.todo_backend.http.todo

import cats.syntax.all._
import cats.effect.{Concurrent, ContextShift, Timer}
import org.http4s.HttpRoutes
import sttp.tapir.server.http4s.Http4sServerInterpreter

object TodoHttp4sRoutes {

  def apply[
    F[_]
    : Concurrent
    : ContextShift
    : Timer
  ](
    todoHandler: TodoHandler[F]
  ): HttpRoutes[F] = {

    val getAllTodoItems: HttpRoutes[F] =
      Http4sServerInterpreter.toRoutes(
        endpoints.getAllTodoItems
      )(_ => todoHandler.getAllTodoItems())

    val getTodoItemById: HttpRoutes[F] =
      Http4sServerInterpreter.toRoutes(
        endpoints.getTodoItemById
      )(todoHandler.getTodoItemById _)

    val createTodoItem: HttpRoutes[F] =
      Http4sServerInterpreter.toRoutes(
        endpoints.createTodoItem
      )(todoHandler.createTodoItem _)

    val changeTodoItemById: HttpRoutes[F] =
      Http4sServerInterpreter.toRoutes(
        endpoints.changeTodoItemById
      )(todoHandler.changeTodoItemById _)

    val deleteAllTodoItems: HttpRoutes[F] =
      Http4sServerInterpreter.toRoutes(
        endpoints.deleteAllTodoItems
      )(_ => todoHandler.deleteAllTodoItems())

    val deleteTodoItemById: HttpRoutes[F] =
      Http4sServerInterpreter.toRoutes(
        endpoints.deleteTodoItemById
      )(todoHandler.deleteTodoItemById _)

          getAllTodoItems
      <+> getTodoItemById
      <+> createTodoItem
      <+> changeTodoItemById
      <+> deleteAllTodoItems
      <+> deleteTodoItemById
  }
}
