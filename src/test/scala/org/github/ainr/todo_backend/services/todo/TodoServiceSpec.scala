package org.github.ainr.todo_backend.services.todo

import cats.Id as IO
import cats.syntax.all._
import org.github.ainr.todo_backend.domain.{Id, TodoItem, TodoPayload}
import org.github.ainr.todo_backend.repositories.todo.TodoRepo
import org.github.ainr.todo_backend.services.todo.TodoService.{TodoItemNotFound, TodoServiceImpl}
import org.github.ainr.todo_backend.services.todo.mocks.LoggerMock
import org.scalamock.scalatest.MockFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class TodoServiceSpec extends AnyFlatSpec with Matchers with MockFactory with LoggerMock {

  private trait mocks {
    val repo = mock[TodoRepo[IO]]

    val service = new TodoServiceImpl[IO](repo)
  }

  "TodoService.getAllTodoItems" should "return all todo items" in new mocks {
    (logger.info: String => Unit).stubs(*).returning(())
    (repo.getAllTodoItems _).expects().returning(
      TodoItem(
        Id(42),
        TodoPayload(
          title = "Title",
          completed = false,
          ordering = None
        )
      ) :: Nil
    )

    val result = service.getAllTodoItems()

    result shouldBe TodoItem(
      Id(42),
      TodoPayload(
        title = "Title",
        completed = false,
        ordering = None
      )
    ) :: Nil
  }

  it should "return empty todo items" in new mocks {
    (logger.info: String => Unit).stubs(*).returning(())
    (repo.getAllTodoItems _).expects().returning(Nil)

    val result = service.getAllTodoItems()

    result shouldBe Nil
  }

  "TodoService.getTodoItemById" should "return todo item by id" in new mocks {
    val id = Id(42)
    (logger.info: String => Unit).stubs(*).returning(())
    (repo.getTodoItemById _).expects(*).returning(
      TodoItem(
        id, TodoPayload(
          title = "Title",
          completed = false,
          ordering = None
        )
      ).some
    )

    val result = service.getTodoItemById(id)

    result shouldBe TodoItem(
      id, TodoPayload(
        title = "Title",
        completed = false,
        ordering = None
      )
    ).some
  }

  "TodoService.getTodoItemById" should "return None" in new mocks {
    (logger.info: String => Unit).stubs(*).returning(())
    (repo.getTodoItemById _).expects(*).returning(None)

    val result = service.getTodoItemById(Id(42))

    result shouldBe None
  }

  "TodoService.createTodoItem" should "create todo item" in new mocks {
    val id = Id(42)
    val todoPayload = TodoPayload(
      title = "Title",
      completed = false,
      ordering = None
    )

    (logger.info: String => Unit).stubs(*).returning(())
    (repo.createTodoItem _).expects(*).returning(TodoItem(id, todoPayload))

    val result = service.createTodoItem(todoPayload)

    result shouldBe TodoItem(id, todoPayload)
  }

  "TodoService.changeTodoItemById" should "change todo item" in new mocks {
    val id = Id(42)
    val todoPayload = TodoPayload(title = "Current title", completed = false, ordering = None)
    (repo.getTodoItemById _).expects(*).returning(TodoItem(id, todoPayload).some)

    (logger.info: String => Unit).stubs(*).returning(())
    (repo.updateTodoItem _).expects(*).returning(())

    val result = service.changeTodoItemById(id, "New title".some, true.some, 73.some)

    result shouldBe Right(TodoItem(id, TodoPayload(title = "New title", completed = true, ordering = 73.some)))
  }

  "TodoService.changeTodoItemById" should "not change todo item if arg is None" in new mocks {
    val id = Id(42)
    val todoPayload = TodoPayload(title = "Current title", completed = false, ordering = None)
    (repo.getTodoItemById _).expects(*).returning(TodoItem(id, todoPayload).some)

    (logger.info: String => Unit).stubs(*).returning(())
    (repo.updateTodoItem _).expects(*).returning(())

    val result = service.changeTodoItemById(id, None, None, None)

    result shouldBe Right(TodoItem(id, todoPayload))
  }

  "TodoService.changeTodoItemById" should "return TodoItemNotFound" in new mocks {
    (repo.getTodoItemById _).expects(*).returning(None)
    (logger.info: String => Unit).stubs(*).returning(())

    val result = service.changeTodoItemById(Id(42), "New title".some, true.some, 73.some)

    result shouldBe Left(TodoItemNotFound)
  }

  "TodoService.deleteAllTodoItems" should "delete all todo items" in new mocks {
    (repo.deleteAllTodoItems _).expects().returning(())
    (logger.info: String => Unit).stubs(*).returning(())

    val result = service.deleteAllTodoItems()

    result shouldBe ()
  }

  "TodoService.deleteTodoItemById" should "delete todo item by id" in new mocks {
    (logger.info: String => Unit).stubs(*).returning(())
    (repo.deleteTodoItemById _).expects(*).returning(())
    (repo.getTodoItemById _).expects(*).returning(
      TodoItem(
        Id(42), TodoPayload(
          title = "Title",
          completed = false,
          ordering = None
        )
      ).some
    )

    val result = service.deleteTodoItemById(Id(42))

    result shouldBe Right(())
  }

  "TodoService.deleteTodoItemById" should "return TodoItemNotFound" in new mocks {
    (repo.getTodoItemById _).expects(*).returning(None)

    val result = service.deleteTodoItemById(Id(42))

    result shouldBe Left(TodoItemNotFound)
  }
}
