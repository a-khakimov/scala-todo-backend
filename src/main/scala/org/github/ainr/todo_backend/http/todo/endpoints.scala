package org.github.ainr.todo_backend.http.todo

import io.circe.generic.auto._
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.openapi.Server
import sttp.tapir.openapi.circe.yaml.RichOpenAPI

object endpoints {

  sealed trait ErrorInfo
  case object NotFound extends ErrorInfo
  case object NoContent extends ErrorInfo
  case object Unknown extends ErrorInfo

  val baseEndpoint: Endpoint[Unit, ErrorInfo, Unit, Any] = endpoint.errorOut(
    oneOf[ErrorInfo](
      statusMapping(StatusCode.NotFound, emptyOutput.map(_ => NoContent)(_ => ())),
      statusMapping(StatusCode.NoContent, emptyOutput.map(_ => NoContent)(_ => ())),
      statusDefaultMapping(emptyOutput.map(_ => NoContent)(_ => ()))
    )
  )

  val getAllTodoItems: Endpoint[Unit, ErrorInfo, List[TodoResponse], Any] =
    baseEndpoint
      .get
      .out(anyJsonBody[List[TodoResponse]])

  val getTodoItemById: Endpoint[Long, ErrorInfo, TodoResponse, Any] =
    baseEndpoint
      .get
      .in(path[Long])
      .out(jsonBody[TodoResponse])

  val createTodoItem: Endpoint[TodoPostRequest, ErrorInfo, TodoResponse, Any] =
    baseEndpoint
      .post
      .in(jsonBody[TodoPostRequest])
      .out(jsonBody[TodoResponse])

  val changeTodoItemById: Endpoint[(Long, TodoPatchRequest), ErrorInfo, TodoResponse, Any] =
    baseEndpoint
      .patch
      .in(path[Long])
      .in(jsonBody[TodoPatchRequest])
      .out(jsonBody[TodoResponse])

  val deleteAllTodoItems: Endpoint[Unit, ErrorInfo, Unit, Any] =
    baseEndpoint
      .delete

  val deleteTodoItemById: Endpoint[Long, ErrorInfo, Unit, Any] =
    baseEndpoint
      .delete
      .in(path[Long])

  val docs = OpenAPIDocsInterpreter.toOpenAPI(
    List(
      getAllTodoItems,
      getTodoItemById,
      createTodoItem,
      changeTodoItemById,
      deleteAllTodoItems,
      deleteTodoItemById
    ),
    "Todo Backend",
    "1.0"
  ).servers(
    List(
      Server("http://localhost:5555/api/").description("test")
    )
  ).toYaml
}
