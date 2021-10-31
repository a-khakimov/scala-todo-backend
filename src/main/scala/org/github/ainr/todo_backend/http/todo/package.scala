package org.github.ainr.todo_backend.http

import org.github.ainr.todo_backend.domain.{TodoItem, TodoPayload}

package object todo {

  final case class CreateTodoItemRequest(
    title: String,
    order: Option[Int] = None
  ) {
    def asTodoPayload: TodoPayload = TodoPayload(title, completed = false, order)
  }

  final case class ChangeTodoItemRequest(
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
        s"$path/${item.id}",
        item.payload.title,
        item.payload.completed,
        item.payload.ordering,
      )
  }
}
