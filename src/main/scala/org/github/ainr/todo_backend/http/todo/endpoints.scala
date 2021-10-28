package org.github.ainr.todo_backend.http.todo

import io.circe.generic.auto.*
import org.github.ainr.todo_backend.http.interpreter.HandlerImpl.TodoResponse
import sttp.model.StatusCode
import sttp.tapir.*
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.generic.auto.*
import sttp.tapir.json.circe.*
import sttp.tapir.openapi.OpenAPI

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

  val docs: OpenAPI = OpenAPIDocsInterpreter.toOpenAPI(getAllTodoItems, "Todo Backend", "1.0")
}
