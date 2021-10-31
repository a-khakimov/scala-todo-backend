package org.github.ainr.todo_backend.http.todo

import io.circe.generic.auto._
import org.github.ainr.todo_backend.config.Http
import org.github.ainr.todo_backend.domain.Id
import sttp.model.StatusCode
import sttp.tapir._
import sttp.tapir.CodecFormat.TextPlain
import sttp.tapir.docs.openapi.OpenAPIDocsInterpreter
import sttp.tapir.generic.auto._
import sttp.tapir.json.circe._
import sttp.tapir.openapi.Server
import sttp.tapir.openapi.circe.yaml.RichOpenAPI

import scala.util.{Failure, Success}

object endpoints {

  sealed trait ErrorInfo
  case object NotFound extends ErrorInfo
  case object NoContent extends ErrorInfo
  case object Unknown extends ErrorInfo

  private val baseEndpoint: Endpoint[Unit, ErrorInfo, Unit, Any] = endpoint.errorOut(
    oneOf[ErrorInfo](
      statusMapping(StatusCode.NotFound, emptyOutput.map(_ => NotFound)(_ => ())),
      statusMapping(StatusCode.NoContent, emptyOutput.map(_ => NoContent)(_ => ())),
      statusDefaultMapping(emptyOutput.map(_ => Unknown)(_ => ()))
    )
  )

  private implicit val idCodec: Codec[String, Id, TextPlain] =
    Codec.string.mapDecode(input => Id.parse(input) match {
      case Success(value) => DecodeResult.Value(value)
      case Failure(cause) => DecodeResult.Error(input, cause)
    })(id => id.toString)

  val getAllTodoItems: Endpoint[Unit, ErrorInfo, List[TodoResponse], Any] =
    baseEndpoint
      .description("Get all todo items")
      .get
      .out(anyJsonBody[List[TodoResponse]])

  val getTodoItemById: Endpoint[Id, ErrorInfo, TodoResponse, Any] =
    baseEndpoint
      .description("Get todo item by id")
      .get
      .in(path[Id]("id"))
      .out(jsonBody[TodoResponse])

  val createTodoItem: Endpoint[CreateTodoItemRequest, ErrorInfo, TodoResponse, Any] =
    baseEndpoint
      .description("Create todo item")
      .post
      .in(jsonBody[CreateTodoItemRequest])
      .out(jsonBody[TodoResponse])

  val changeTodoItemById: Endpoint[(Id, ChangeTodoItemRequest), ErrorInfo, TodoResponse, Any] =
    baseEndpoint
      .description("Change todo item by id")
      .patch
      .in(path[Id]("id"))
      .in(jsonBody[ChangeTodoItemRequest])
      .out(jsonBody[TodoResponse])

  val deleteAllTodoItems: Endpoint[Unit, ErrorInfo, Unit, Any] =
    baseEndpoint
      .description("Delete all todo items")
      .delete

  val deleteTodoItemById: Endpoint[Id, ErrorInfo, Unit, Any] =
    baseEndpoint
      .description("Delete todo item by id")
      .delete
      .in(path[Id]("id"))

  def openApiYaml(conf: Http.Config): String =
    OpenAPIDocsInterpreter.toOpenAPI(
      List(
        getAllTodoItems,
        getTodoItemById,
        createTodoItem,
        changeTodoItemById,
        deleteAllTodoItems,
        deleteTodoItemById
      ),
      "Todo Backend Api (Scala)",
      conf.version
    ).servers(
      List(Server(s"http://${conf.host}:${conf.port}/api"))
    ).toYaml
}
