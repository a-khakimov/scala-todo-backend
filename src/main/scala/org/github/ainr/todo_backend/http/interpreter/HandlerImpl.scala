package org.github.ainr.todo_backend.http.interpreter

import cats.effect.Sync
import cats.syntax.all.*
import io.circe.generic.auto.*
import io.circe.syntax.*
import org.github.ainr.todo_backend.domain.{Id, TodoItem, TodoPayload}
import org.github.ainr.todo_backend.http.Handler
import org.github.ainr.todo_backend.http.interpreter.HandlerImpl.{TodoPatchRequest, TodoPostRequest, TodoResponse}
import org.github.ainr.todo_backend.services.healthcheck.HealthCheckService
import org.github.ainr.todo_backend.services.todo.TodoService
import org.github.ainr.todo_backend.services.version.VersionService
import org.http4s.HttpRoutes
import org.http4s.circe.*
import org.http4s.circe.CirceEntityCodec.circeEntityDecoder
import org.http4s.dsl.Http4sDsl

object HandlerImpl {

  final case class TodoPostRequest(title: String, order: Option[Int] = None) {

    def asTodoItem(id: Id): TodoItem = TodoItem(id, this.asTodoPayload)

    def asTodoPayload: TodoPayload = TodoPayload(title, completed = false, order)
  }

  final case class TodoPatchRequest(
    title: Option[String] = None,
    completed: Option[Boolean] = None,
    order: Option[Int] = None
  )

  final case class TodoResponse(
    id: Long,
    url: String,
    title: String,
    completed: Boolean,
    order: Option[Int]
  )

  object TodoResponse {
    def apply(path: String, item: TodoItem): TodoResponse =
      TodoResponse(
        item.id.value,
        s"$path/${item.id.value}",
        item.payload.title,
        item.payload.completed,
        item.payload.ordering,
      )
  }
}

final class HandlerImpl[
  F[_]
  : Sync
](
   todoService: TodoService[F],
   healthCheckService: HealthCheckService[F],
   versionService: VersionService[F]
 ) extends Handler[F] {

  object dsl extends Http4sDsl[F]
  import dsl.*

  override def routes: HttpRoutes[F] = HttpRoutes.of[F] {

    case GET -> Root =>
      todoService
        .getAllTodoItems
        .flatMap(items => Ok(items.map(item => TodoResponse("http://localhost:5555/api", item)).asJson))

    case GET -> Root / LongVar(id) =>
      todoService
        .getTodoItemById(Id(id))
        .flatMap { itemOpt =>
          itemOpt.fold(NotFound())(item => Ok(TodoResponse("http://localhost:5555/api", item).asJson))
        }

    case req @ POST -> Root =>
      req.decode[TodoPostRequest] { request =>
        todoService
          .createTodoItem(request.asTodoPayload)
          .map(item => TodoResponse("http://localhost:5555/api", item).asJson)
          .flatMap(Created(_))
      }

    case req @ PATCH -> Root / LongVar(id) =>
      req.decode[TodoPatchRequest] { request =>
        todoService
          .changeTodoItemById(Id(id), request.title, request.completed, request.order)
          .flatMap {
            case Right(item) => Ok(TodoResponse("http://localhost:5555/api", item).asJson)
            case Left(_) => NotFound()
          }
      }

    case DELETE -> Root =>
      todoService
        .deleteAllTodoItems()
        *> Ok()

    case DELETE -> Root / LongVar(id) => for {
      itemOpt <- todoService.getTodoItemById(Id(id))
      response <- itemOpt.fold(NotFound())(
        _ => todoService
          .deleteTodoItemById(Id(id))
          .flatMap(Ok(_))
      )
    } yield response

    case GET -> Root / "version" =>
      versionService
        .version()
        .flatMap(v => Ok(v.asJson))

    case GET -> Root / "health_check" =>
      healthCheckService
        .healthCheck()
        .flatMap(h => Ok(h.asJson))
  }
}