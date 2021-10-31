package org.github.ainr.todo_backend.http.todo

import cats.implicits.catsSyntaxOptionId
import org.github.ainr.todo_backend.domain.{Id, TodoItem, TodoPayload}
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import sttp.client3.monad.IdMonad
import sttp.client3.testing.SttpBackendStub
import sttp.client3.{Identity, UriContext, basicRequest}
import sttp.tapir.server.stub.RichSttpBackendStub


class EndpointsSpec extends AnyFlatSpec with Matchers with MockFactory {

  val todoResponse = TodoResponse("path", TodoItem(Id(42), TodoPayload("Title", false, 42.some)))

  val baseBackendStub: SttpBackendStub[Identity, Nothing] =
    SttpBackendStub
      .apply(IdMonad)

  behavior of "Todo Api endpoints"

  it should "getAllTodoItems" in {
    val backend = baseBackendStub
      .whenRequestMatchesEndpoint(endpoints.getAllTodoItems)
      .thenSuccess(todoResponse :: Nil)

    val result = basicRequest
      .get(uri"http://fake")
      .send(backend)

    result.body shouldBe Right("""[{"id":42,"url":"path/42","title":"Title","completed":false,"order":42}]""")
  }

  it should "getTodoItemById" in {
    val backend = baseBackendStub
      .whenRequestMatchesEndpoint(endpoints.getTodoItemById)
      .thenSuccess(todoResponse)

    val result = basicRequest
      .get(uri"http://fake/42")
      .send(backend)

    result.body shouldBe Right("""{"id":42,"url":"path/42","title":"Title","completed":false,"order":42}""")
  }

  it should "createTodoItem" in {
    val backend = baseBackendStub
      .whenRequestMatchesEndpoint(endpoints.createTodoItem)
      .thenSuccess(todoResponse)

    val result = basicRequest
      .post(uri"http://fake/")
      .send(backend)

    result.body shouldBe Right("""{"id":42,"url":"path/42","title":"Title","completed":false,"order":42}""")
  }

  it should "changeTodoItemById" in {
    val backend = baseBackendStub
      .whenRequestMatchesEndpoint(endpoints.changeTodoItemById)
      .thenSuccess(todoResponse)

    val result = basicRequest
      .patch(uri"http://fake/42")
      .body()
      .send(backend)

    result.body shouldBe Right("""{"id":42,"url":"path/42","title":"Title","completed":false,"order":42}""")
  }

  it should "deleteAllTodoItems" in {
    val backend = baseBackendStub
      .whenRequestMatchesEndpoint(endpoints.deleteAllTodoItems)
      .thenSuccess(())

    val result = basicRequest
      .delete(uri"http://fake/")
      .send(backend)

    result.body shouldBe()
  }

  it should "deleteTodoItemById" in {
    val backend = baseBackendStub
      .whenRequestMatchesEndpoint(endpoints.deleteTodoItemById)
      .thenSuccess(())

    val result = basicRequest
      .delete(uri"http://fake/42")
      .send(backend)

    result.body shouldBe()
  }
}
