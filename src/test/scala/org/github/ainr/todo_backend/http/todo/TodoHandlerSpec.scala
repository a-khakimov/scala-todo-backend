package org.github.ainr.todo_backend.http.todo

import cats.{Id => IO}
import cats.implicits.catsSyntaxOptionId
import org.github.ainr.todo_backend.config.Http
import org.github.ainr.todo_backend.domain.{Id, TodoItem, TodoPayload}
import org.github.ainr.todo_backend.http.todo.endpoints.NotFound
import org.github.ainr.todo_backend.services.todo.TodoService
import org.github.ainr.todo_backend.services.todo.TodoService.TodoItemNotFound
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TodoHandlerSpec extends AnyFlatSpec with Matchers with MockFactory {

  private trait mocks {
    val todoService = mock[TodoService[IO]]
    val handler = TodoHandler[IO](todoService, Http.Config(80, "host", "version"))
  }

  "TodoHandler.getAllTodoItems" should "return TodoResponse with all todo items" in new mocks {
    (todoService.getAllTodoItems _).expects().returning(
      TodoItem(
        Id(42),
        TodoPayload(
          title = "Title",
          completed = false,
          ordering = None
        )
      ) :: Nil
    )

    val result = handler.getAllTodoItems()

    result shouldBe Right(
      TodoResponse(
        id = 42,
        url = "http://host:80/api/42",
        title = "Title",
        completed = false,
        order = None
      ) :: Nil
    )
  }

  "TodoHandler.getTodoItemById" should "return TodoResponse with todo item by id" in new mocks {
    (todoService.getTodoItemById _).expects(*).returning(
      TodoItem(
        Id(42),
        TodoPayload(
          title = "Title",
          completed = false,
          ordering = None
        )
      ).some
    )

    val result = handler.getTodoItemById(Id(42))

    result shouldBe Right(
      TodoResponse(
        id = 42,
        url = "http://host:80/api/42",
        title = "Title",
        completed = false,
        order = None
      )
    )
  }

  "TodoHandler.getTodoItemById" should "return NotFound if todo item not exist" in new mocks {
    (todoService.getTodoItemById _).expects(*).returning(None)

    val result = handler.getTodoItemById(Id(42))

    result shouldBe Left(NotFound)
  }

  "TodoHandler.createTodoItem" should "return TodoResponse" in new mocks {
    (todoService.createTodoItem _).expects(*).returning(
      TodoItem(
        Id(42),
        TodoPayload(
          title = "Title",
          completed = false,
          ordering = 73.some
        )
      )
    )

    val result = handler.createTodoItem(
      CreateTodoItemRequest(
        title = "Title",
        order = 73.some
      )
    )

    result shouldBe Right(TodoResponse(42, "http://host:80/api/42", "Title", false, Some(73)))
  }

  "TodoHandler.changeTodoItemById" should "return TodoResponse" in new mocks {
    (todoService.changeTodoItemById _).expects(*, *, *, *)
      .returning(
        Right(
          TodoItem(
            Id(42),
            TodoPayload(
              title = "Current title",
              completed = false,
              ordering = 73.some
            )
          )
        )
    )

    val result = handler.changeTodoItemById((Id(42), ChangeTodoItemRequest(None, None, None)))

    result shouldBe Right(TodoResponse(42, "http://host:80/api/42", "Current title", false, Some(73)))
  }

  "TodoHandler.changeTodoItemById" should "return NotFound" in new mocks {
    (todoService.changeTodoItemById _).expects(*, *, *, *)
      .returning(Left(TodoItemNotFound))

    val result = handler.changeTodoItemById((Id(42), ChangeTodoItemRequest(None, None, None)))

    result shouldBe Left(NotFound)
  }

  "TodoHandler.deleteAllTodoItems" should "delete all todo items" in new mocks {
    (todoService.deleteAllTodoItems _).expects().returning(())

    val result = handler.deleteAllTodoItems()

    result shouldBe Right(())
  }

  "TodoHandler.deleteTodoItemById" should "delete todo item by id" in new mocks {
    (todoService.deleteTodoItemById _).expects(*).returning(Right(()))

    val result = handler.deleteTodoItemById(Id(42))

    result shouldBe Right(())
  }

  "TodoHandler.deleteTodoItemById" should "NotFound" in new mocks {
    (todoService.deleteTodoItemById _).expects(*).returning(Left(TodoItemNotFound))

    val result = handler.deleteTodoItemById(Id(42))

    result shouldBe Left(NotFound)
  }
}
