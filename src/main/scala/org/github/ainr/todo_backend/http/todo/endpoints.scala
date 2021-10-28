package org.github.ainr.todo_backend.http.todo

import io.circe.generic.auto._
import org.github.ainr.todo_backend.http.interpreter.HandlerImpl.{TodoPatchRequest, TodoPostRequest, TodoResponse}
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.openapi.{OpenAPI, Server}
import sttp.tapir.openapi.circe.yaml.RichOpenAPI
import sttp.tapir.swagger.http4s.SwaggerHttp4s

object endpoints {

  sealed trait ErrorInfo
  case class NotFound(what: String) extends ErrorInfo
  case class Unauthorized(realm: String) extends ErrorInfo
  case class Unknown(code: Int, msg: String) extends ErrorInfo
  case object NoContent extends ErrorInfo

  val baseEndpoint = endpoint.errorOut(
    oneOf[ErrorInfo](
      statusMapping(StatusCode.NotFound, jsonBody[NotFound].description("not found")),
      statusMapping(StatusCode.Unauthorized, jsonBody[Unauthorized].description("unauthorized")),
      statusMapping(StatusCode.NoContent, emptyOutput.map(_ => NoContent)(_ => ())),
      statusDefaultMapping(jsonBody[Unknown].description("unknown"))
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

  private val docs: OpenAPI = OpenAPIDocsInterpreter.toOpenAPI(
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
      Server("from config")
        .description("Prod")
    )
  )

  val docsEndpoint = new SwaggerHttp4s(docs.toYaml)
}
